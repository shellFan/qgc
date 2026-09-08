package com.qiongguichou.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayRefundV3Request;
import com.github.binarywang.wxpay.bean.result.WxPayRefundV3Result;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.service.WxPayService;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.enums.PaymentStatus;
import com.qiongguichou.common.enums.RefundStatus;
import com.qiongguichou.common.enums.SupportStatus;
import com.qiongguichou.common.event.PaySuccessEvent;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.util.OrderNoUtil;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.entity.ShareRecord;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.campaign.mapper.ShareRecordMapper;
import com.qiongguichou.payment.config.QgcWxPayConfig;
import com.qiongguichou.payment.dto.PayRequest;
import com.qiongguichou.payment.dto.PayResult;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.entity.RefundOrder;
import com.qiongguichou.payment.entity.SupportOrder;
import com.qiongguichou.payment.mapper.PaymentOrderMapper;
import com.qiongguichou.payment.mapper.RefundOrderMapper;
import com.qiongguichou.payment.mapper.SupportOrderMapper;
import com.qiongguichou.payment.service.PaymentService;
import com.qiongguichou.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务实现
 * 核心流程：Redis分布式锁 → 创建支持订单 → 创建支付订单 → 调用微信支付 → 回调确认 → 超额退款
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final SupportOrderMapper supportOrderMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final CampaignMapper campaignMapper;
    private final ShareRecordMapper shareRecordMapper;
    private final RedissonClient redissonClient;
    private final QgcWxPayConfig qgcWxPayConfig;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;

    /** 微信支付服务，Mock模式下为null */
    private WxPayService wxPayService;

    /**
     * 发起支付
     * 1. 获取分布式锁(防止超额)
     * 2. 校验筹款状态和剩余金额
     * 3. 创建支持订单+支付订单
     * 4. 调用微信统一下单(或Mock模式直接成功)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayResult pay(Long userId, String openid, PayRequest request) {
        Long campaignId = request.getCampaignId();
        Long amount = request.getAmount();

        // 1. 分布式锁 - 防止并发超额
        String lockKey = "qgc:campaign:pay:" + campaignId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.SYSTEM_BUSY);
            }

            // 2. 校验筹款
            Campaign campaign = campaignMapper.selectById(campaignId);
            if (campaign == null) {
                throw new BusinessException(ErrorCode.CAMPAIGN_NOT_FOUND);
            }
            if (!CampaignStatus.ACTIVE.name().equals(campaign.getStatus())) {
                throw new BusinessException(ErrorCode.CAMPAIGN_NOT_ACTIVE);
            }
            // 不能投喂自己的筹款
            if (campaign.getCreatorUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "不能投喂自己的筹款");
            }

            // RC5: requestId幂等检查 - 防止客户端重复提交
            String requestId = request.getRequestId();
            if (requestId != null && !requestId.isEmpty()) {
                SupportOrder existing = supportOrderMapper.selectOne(
                        new LambdaQueryWrapper<SupportOrder>()
                                .eq(SupportOrder::getRequestId, requestId)
                                .last("LIMIT 1"));
                if (existing != null) {
                    log.info("支付请求幂等跳过: requestId={}, supportNo={}", requestId, existing.getSupportNo());
                    PayResult existResult = new PayResult();
                    existResult.setOrderNo(existing.getSupportNo());
                    return existResult;
                }
            }

            // RC5: shareCode归因验证 - 确保分享码属于当前筹款
            String shareCode = request.getShareCode();
            if (shareCode != null && !shareCode.isEmpty()) {
                ShareRecord shareRecord = shareRecordMapper.selectOne(
                        new LambdaQueryWrapper<ShareRecord>()
                                .eq(ShareRecord::getShareCode, shareCode)
                                .last("LIMIT 1"));
                if (shareRecord == null || !shareRecord.getCampaignId().equals(campaignId)) {
                    log.warn("Share归因校验失败: shareCode={}, campaignId={}, 归属campaignId={}",
                            shareCode, campaignId, shareRecord != null ? shareRecord.getCampaignId() : null);
                    // 不阻断支付，仅记录警告，清除shareCode防止错误归因
                    request.setShareCode(null);
                }
            }
            Long remaining = campaign.getTargetAmount() - campaign.getRaisedAmount();
            if (remaining <= 0) {
                throw new BusinessException(ErrorCode.CAMPAIGN_FULL);
            }
            if (amount > remaining) {
                throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_EXCEED,
                        "剩余可投喂" + com.qiongguichou.common.util.MoneyUtil.fenToYuanStr(remaining) + "元");
            }

            // 3. 创建支持订单
            SupportOrder supportOrder = new SupportOrder();
            supportOrder.setSupportNo(OrderNoUtil.supportNo());
            supportOrder.setCampaignId(campaignId);
            supportOrder.setSupporterUserId(userId);
            supportOrder.setCreatorUserId(campaign.getCreatorUserId());
            supportOrder.setAmount(amount);
            supportOrder.setEffectiveAmount(0L);
            supportOrder.setRefundAmount(0L);
            supportOrder.setMessage(request.getMessage());
            supportOrder.setAnonymous(request.getAnonymous() != null ? request.getAnonymous() : 0);
            supportOrder.setHideAmount(0);
            supportOrder.setStatus(SupportStatus.CREATED.name());
            supportOrder.setRequestId(requestId);
            supportOrderMapper.insert(supportOrder);

            // 4. 创建支付订单
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setOrderNo(OrderNoUtil.paymentNo());
            paymentOrder.setSupportNo(supportOrder.getSupportNo());
            paymentOrder.setCampaignId(campaignId);
            paymentOrder.setSupporterUserId(userId);
            paymentOrder.setCreatorUserId(campaign.getCreatorUserId());
            paymentOrder.setAmount(amount);
            paymentOrder.setEffectiveAmount(0L);
            paymentOrder.setRefundAmount(0L);
            paymentOrder.setTradeType("JSAPI");
            paymentOrder.setOpenid(openid);
            paymentOrder.setStatus(PaymentStatus.CREATED.name());
            paymentOrderMapper.insert(paymentOrder);

            // 5. 调用微信支付或Mock
            PayResult result = new PayResult();
            result.setOrderNo(supportOrder.getSupportNo());
            result.setPaymentOrderNo(paymentOrder.getOrderNo());

            if (qgcWxPayConfig.isMockEnabled()) {
                // Mock模式：直接返回成功，模拟支付回调
                handleMockPaySuccess(paymentOrder, supportOrder, campaign);
                result.setWxPayParams(null);
            } else {
                // 真实微信支付
                WxPayMpOrderResult wxResult = createWxOrder(paymentOrder, openid);
                PayResult.WxPayParams params = new PayResult.WxPayParams();
                params.setAppId(wxResult.getAppId());
                params.setTimeStamp(wxResult.getTimeStamp());
                params.setNonceStr(wxResult.getNonceStr());
                params.setPackageValue(wxResult.getPackageValue());
                params.setSignType(wxResult.getSignType());
                params.setPaySign(wxResult.getPaySign());
                result.setWxPayParams(params);
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_BUSY);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    /**
     * 微信支付回调处理
     * 幂等：transaction_id唯一索引 + 订单状态判断
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePayNotify(String notifyData) {
        if (qgcWxPayConfig.isMockEnabled()) {
            return "SUCCESS";
        }

        try {
            WxPayNotifyV3Result notifyResult = wxPayService.parseOrderNotifyV3Result(notifyData, null);
            WxPayNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();

            // 幂等检查：根据transaction_id查找支付订单
            PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>()
                            .eq(PaymentOrder::getTransactionId, result.getTransactionId())
                            .last("LIMIT 1"));
            if (paymentOrder != null && PaymentStatus.SUCCESS.name().equals(paymentOrder.getStatus())) {
                // 已处理过，直接返回成功
                return "SUCCESS";
            }

            // 根据订单号查找
            paymentOrder = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>()
                            .eq(PaymentOrder::getOrderNo, result.getOutTradeNo())
                            .last("LIMIT 1"));
            if (paymentOrder == null) {
                log.error("支付回调订单不存在: {}", result.getOutTradeNo());
                return "FAIL";
            }

            // 幂等：状态必须是CREATED或PAYING
            if (!PaymentStatus.CREATED.name().equals(paymentOrder.getStatus())
                    && !PaymentStatus.PAYING.name().equals(paymentOrder.getStatus())) {
                return "SUCCESS";
            }

            // 金额校验：回调金额必须与订单金额一致
            Long paidAmount = Long.parseLong(result.getAmount().getTotal() + "");
            if (!paidAmount.equals(paymentOrder.getAmount())) {
                log.error("支付回调金额不匹配: 订单金额={}, 回调金额={}", paymentOrder.getAmount(), paidAmount);
                return "FAIL";
            }

            // 更新支付订单(DB条件更新: WHERE status IN ('CREATED','PAYING') 保证只有一个线程入账)
            LambdaUpdateWrapper<PaymentOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PaymentOrder::getId, paymentOrder.getId())
                    .in(PaymentOrder::getStatus, PaymentStatus.CREATED.name(), PaymentStatus.PAYING.name())
                    .set(PaymentOrder::getStatus, PaymentStatus.SUCCESS.name())
                    .set(PaymentOrder::getEffectiveAmount, paidAmount)
                    .set(PaymentOrder::getTransactionId, result.getTransactionId())
                    .set(PaymentOrder::getPayTime, LocalDateTime.now())
                    .set(PaymentOrder::getNotifyTime, LocalDateTime.now());
            int rows = paymentOrderMapper.update(null, updateWrapper);
            if (rows == 0) {
                // 并发场景下其他线程已处理, 直接返回成功
                log.info("支付回调并发跳过: orderId={}", paymentOrder.getId());
                return "SUCCESS";
            }

            // 重新查询更新后的订单
            paymentOrder = paymentOrderMapper.selectById(paymentOrder.getId());

            // 处理支付成功
            processPaySuccess(paymentOrder);

            return "SUCCESS";
        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            return "FAIL";
        }
    }

    /**
     * 微信退款回调处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleRefundNotify(String notifyData) {
        if (qgcWxPayConfig.isMockEnabled()) {
            return "SUCCESS";
        }

        try {
            WxPayRefundNotifyV3Result notifyResult = wxPayService.parseRefundNotifyV3Result(notifyData, null);
            WxPayRefundNotifyV3Result.DecryptNotifyResult result = notifyResult.getResult();

            RefundOrder refundOrder = refundOrderMapper.selectOne(
                    new LambdaQueryWrapper<RefundOrder>()
                            .eq(RefundOrder::getRefundNo, result.getOutRefundNo())
                            .last("LIMIT 1"));
            if (refundOrder == null) {
                log.error("退款回调订单不存在: {}", result.getOutRefundNo());
                return "FAIL";
            }

            if ("SUCCESS".equals(result.getRefundStatus())) {
                refundOrder.setStatus(RefundStatus.SUCCESS.name());
                refundOrder.setWechatRefundId(result.getTransactionId());
                refundOrder.setRefundTime(LocalDateTime.now());
            } else {
                refundOrder.setStatus(RefundStatus.FAIL.name());
            }
            refundOrderMapper.updateById(refundOrder);

            return "SUCCESS";
        } catch (Exception e) {
            log.error("退款回调处理异常", e);
            return "FAIL";
        }
    }

    /**
     * 超额自动退款
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRefundForOverpay(Long paymentOrderId, Long refundAmount, String reason) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectById(paymentOrderId);
        if (paymentOrder == null) {
            return;
        }

        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundNo(OrderNoUtil.refundNo());
        refundOrder.setPaymentOrderNo(paymentOrder.getOrderNo());
        refundOrder.setCampaignId(paymentOrder.getCampaignId());
        refundOrder.setUserId(paymentOrder.getSupporterUserId());
        refundOrder.setAmount(refundAmount);
        refundOrder.setReason(reason);
        refundOrder.setType("OVERPAY");
        refundOrder.setStatus(RefundStatus.PENDING.name());
        refundOrderMapper.insert(refundOrder);

        // 更新支付订单状态
        paymentOrder.setStatus(PaymentStatus.REFUNDING.name());
        paymentOrderMapper.updateById(paymentOrder);

        // 调用微信退款或Mock
        if (qgcWxPayConfig.isMockEnabled()) {
            refundOrder.setStatus(RefundStatus.SUCCESS.name());
            refundOrder.setRefundTime(LocalDateTime.now());
            refundOrderMapper.updateById(refundOrder);
            paymentOrder.setStatus(PaymentStatus.PART_REFUNDED.name());
            paymentOrderMapper.updateById(paymentOrder);
        } else {
            doWxRefund(paymentOrder, refundOrder);
        }
    }

    /**
     * 关闭超时未支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredOrder(Long paymentOrderId) {
        PaymentOrder paymentOrder = paymentOrderMapper.selectById(paymentOrderId);
        if (paymentOrder == null || !PaymentStatus.CREATED.name().equals(paymentOrder.getStatus())) {
            return;
        }

        // 关闭微信订单
        if (!qgcWxPayConfig.isMockEnabled()) {
            try {
                wxPayService.closeOrder(paymentOrder.getOrderNo());
            } catch (Exception e) {
                log.warn("关闭微信订单失败: {}", paymentOrder.getOrderNo(), e);
            }
        }

        // 更新支付订单状态
        paymentOrder.setStatus(PaymentStatus.CLOSED.name());
        paymentOrderMapper.updateById(paymentOrder);

        // 更新关联支持订单: CREATED → CLOSED
        SupportOrder supportOrder = supportOrderMapper.selectOne(
                new LambdaQueryWrapper<SupportOrder>()
                        .eq(SupportOrder::getSupportNo, paymentOrder.getSupportNo())
                        .last("LIMIT 1"));
        if (supportOrder != null && SupportStatus.CREATED.name().equals(supportOrder.getStatus())) {
            supportOrder.setStatus(SupportStatus.CLOSED.name());
            supportOrderMapper.updateById(supportOrder);
        }
    }

    // ========== 内部方法 ==========

    /**
     * 处理支付成功：更新支持订单 + 更新筹款已筹金额 + 钱包入账 + 检查超额退款
     *
     * 关键：钱包入账必须在同一事务内，确保payment/campaign/wallet数据一致性
     * AFTER_COMMIT事件仅处理缓存清理等非关键逻辑
     */
    private void processPaySuccess(PaymentOrder paymentOrder) {
        // 1. 更新支持订单
        SupportOrder supportOrder = supportOrderMapper.selectOne(
                new LambdaQueryWrapper<SupportOrder>()
                        .eq(SupportOrder::getSupportNo, paymentOrder.getSupportNo())
                        .last("LIMIT 1"));
        if (supportOrder != null) {
            supportOrder.setStatus(SupportStatus.PAID.name());
            supportOrder.setEffectiveAmount(paymentOrder.getEffectiveAmount());
            supportOrder.setPayTime(paymentOrder.getPayTime());
            supportOrderMapper.updateById(supportOrder);
        }

        // 2. 更新筹款已筹金额(使用SQL原子更新)
        Long paidAmount = paymentOrder.getEffectiveAmount();
        campaignMapper.updateRaisedAmount(paymentOrder.getCampaignId(), paidAmount);

        // 3. 钱包入账(必须在事务内，确保与payment/campaign一致)
        Campaign campaign = campaignMapper.selectById(paymentOrder.getCampaignId());
        if (campaign != null) {
            try {
                walletService.campaignIncome(
                        campaign.getCreatorUserId(),
                        paidAmount,
                        campaign.getId(),
                        campaign.getTitle()
                );
            } catch (Exception e) {
                // 钱包入账失败则回滚整个事务，避免payment成功但wallet未入账
                log.error("钱包入账失败，事务将回滚: campaignId={}, creatorUserId={}, amount={}",
                        campaign.getId(), campaign.getCreatorUserId(), paidAmount, e);
                throw e;
            }
        }

        // 4. 检查超额并自动退款
        if (campaign != null && campaign.getRaisedAmount() > campaign.getTargetAmount()) {
            Long overAmount = campaign.getRaisedAmount() - campaign.getTargetAmount();
            // 只退本次支付中超出的部分
            Long refundAmount = Math.min(overAmount, paidAmount);
            if (refundAmount > 0) {
                createRefundForOverpay(paymentOrder.getId(), refundAmount, "筹款已满额，超额部分自动退款");

                // 回退筹款已筹金额
                campaignMapper.updateRaisedAmount(campaign.getId(), -refundAmount);
            }
        }

        // 5. 检查筹款是否已满额，满额则更新状态
        if (campaign != null) {
            Campaign latestCampaign = campaignMapper.selectById(campaign.getId());
            if (latestCampaign != null && latestCampaign.getRaisedAmount().compareTo(latestCampaign.getTargetAmount()) >= 0) {
                Campaign update = new Campaign();
                update.setId(latestCampaign.getId());
                update.setStatus(CampaignStatus.SUCCESS.name());
                campaignMapper.updateById(update);
            }
        }

        // 6. 发布支付成功事件(事务提交后处理缓存清理等非关键逻辑)
        if (campaign != null) {
            eventPublisher.publishEvent(new PaySuccessEvent(
                    this, paymentOrder.getCampaignId(), campaign.getCreatorUserId(),
                    paymentOrder.getSupporterUserId(), paidAmount, campaign.getTitle()));
        }
    }

    /**
     * Mock模式支付成功
     */
    private void handleMockPaySuccess(PaymentOrder paymentOrder, SupportOrder supportOrder, Campaign campaign) {
        paymentOrder.setStatus(PaymentStatus.SUCCESS.name());
        paymentOrder.setEffectiveAmount(paymentOrder.getAmount());
        paymentOrder.setTransactionId("MOCK_" + System.currentTimeMillis());
        paymentOrder.setPayTime(LocalDateTime.now());
        paymentOrder.setNotifyTime(LocalDateTime.now());
        paymentOrderMapper.updateById(paymentOrder);

        processPaySuccess(paymentOrder);
    }

    /**
     * 创建微信统一下单
     */
    private WxPayMpOrderResult createWxOrder(PaymentOrder paymentOrder, String openid) {
        try {
            WxPayUnifiedOrderV3Request request = new WxPayUnifiedOrderV3Request();
            request.setOutTradeNo(paymentOrder.getOrderNo());
            request.setDescription("穷鬼筹-投喂支持");
            request.setAmount(new WxPayUnifiedOrderV3Request.Amount()
                    .setTotal(paymentOrder.getAmount().intValue())
                    .setCurrency("CNY"));
            request.setPayer(new WxPayUnifiedOrderV3Request.Payer().setOpenid(openid));
            request.setNotifyUrl(qgcWxPayConfig.getNotifyUrl());

            return wxPayService.createOrderV3(TradeTypeEnum.JSAPI, request);
        } catch (Exception e) {
            log.error("微信统一下单失败", e);
            throw new BusinessException(ErrorCode.PAYMENT_CREATE_FAIL);
        }
    }

    /**
     * 调用微信退款
     */
    private void doWxRefund(PaymentOrder paymentOrder, RefundOrder refundOrder) {
        try {
            WxPayRefundV3Request request = new WxPayRefundV3Request();
            request.setOutTradeNo(paymentOrder.getOrderNo());
            request.setOutRefundNo(refundOrder.getRefundNo());
            request.setAmount(new WxPayRefundV3Request.Amount()
                    .setRefund(refundOrder.getAmount().intValue())
                    .setTotal(paymentOrder.getEffectiveAmount().intValue())
                    .setCurrency("CNY"));
            request.setReason(refundOrder.getReason());
            request.setNotifyUrl(qgcWxPayConfig.getRefundNotifyUrl());

            WxPayRefundV3Result result = wxPayService.refundV3(request);
            refundOrder.setWechatRefundId(result.getTransactionId());
            refundOrderMapper.updateById(refundOrder);
        } catch (Exception e) {
            log.error("微信退款失败", e);
            refundOrder.setStatus(RefundStatus.FAIL.name());
            refundOrderMapper.updateById(refundOrder);
            paymentOrder.setStatus(PaymentStatus.REFUNDING.name());
            paymentOrderMapper.updateById(paymentOrder);
        }
    }

    // Setter for optional WxPayService injection
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setWxPayService(WxPayService wxPayService) {
        this.wxPayService = wxPayService;
    }

    }