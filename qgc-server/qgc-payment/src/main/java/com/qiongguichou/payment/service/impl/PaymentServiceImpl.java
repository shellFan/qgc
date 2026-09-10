package com.qiongguichou.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.enums.PayType;
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
import com.qiongguichou.payment.entity.PayNotifyLog;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.entity.RefundOrder;
import com.qiongguichou.payment.entity.SupportOrder;
import com.qiongguichou.payment.mapper.PayNotifyLogMapper;
import com.qiongguichou.payment.mapper.PaymentOrderMapper;
import com.qiongguichou.payment.mapper.RefundOrderMapper;
import com.qiongguichou.payment.mapper.SupportOrderMapper;
import com.qiongguichou.payment.provider.NotifyResult;
import com.qiongguichou.payment.provider.PaymentProvider;
import com.qiongguichou.payment.provider.PaymentResult;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务实现 - 基于PaymentProvider架构
 * 核心流程：Redis分布式锁 → 创建支持订单 → 创建支付订单 → PaymentProvider下单 → 回调确认 → 超额退款
 *
 * 支付模式:
 * - MOCK: 开发环境模拟, 直接成功
 * - NATIVE: 扫码支付, 返回code_url, 无需openid
 * - JSAPI: 公众号支付, 需要openid, 仅微信浏览器内
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final SupportOrderMapper supportOrderMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final PayNotifyLogMapper payNotifyLogMapper;
    private final CampaignMapper campaignMapper;
    private final ShareRecordMapper shareRecordMapper;
    private final RedissonClient redissonClient;
    private final QgcWxPayConfig qgcWxPayConfig;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletService walletService;
    private final List<PaymentProvider> paymentProviders;

    /**
     * 发起支付
     * 1. 获取分布式锁(防止超额)
     * 2. 校验筹款状态和剩余金额
     * 3. 创建支持订单+支付订单
     * 4. 调用PaymentProvider下单(或Mock模式直接成功)
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

            // 3. 确定支付类型
            String payType = resolvePayType(request.getPayType());

            // 4. 创建支持订单
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

            // 5. 创建支付订单
            PaymentOrder paymentOrder = new PaymentOrder();
            paymentOrder.setOrderNo(OrderNoUtil.paymentNo());
            paymentOrder.setSupportNo(supportOrder.getSupportNo());
            paymentOrder.setCampaignId(campaignId);
            paymentOrder.setSupporterUserId(userId);
            paymentOrder.setCreatorUserId(campaign.getCreatorUserId());
            paymentOrder.setAmount(amount);
            paymentOrder.setEffectiveAmount(0L);
            paymentOrder.setRefundAmount(0L);
            paymentOrder.setTradeType(payType);
            paymentOrder.setPayType(payType);
            paymentOrder.setOpenid(openid);
            paymentOrder.setRequestId(requestId);
            paymentOrder.setStatus(PaymentStatus.CREATED.name());
            paymentOrderMapper.insert(paymentOrder);

            // 6. 调用PaymentProvider下单
            PayResult result = new PayResult();
            result.setOrderNo(supportOrder.getSupportNo());
            result.setPaymentOrderNo(paymentOrder.getOrderNo());
            result.setPayType(payType);

            if (PayType.MOCK.name().equals(payType)) {
                // Mock模式：直接返回成功，模拟支付回调
                handleMockPaySuccess(paymentOrder, supportOrder, campaign);
                result.setMock(true);
            } else {
                // 真实支付: 调用PaymentProvider
                PaymentResult providerResult = providerFor(payType).createPayment(paymentOrder, openid);

                if (PayType.NATIVE.name().equals(payType)) {
                    // Native支付: 保存codeUrl和过期时间
                    paymentOrder.setCodeUrl(providerResult.getCodeUrl());
                    paymentOrder.setExpireTime(LocalDateTime.parse(providerResult.getExpireTime(),
                            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    paymentOrderMapper.updateById(paymentOrder);
                    result.setCodeUrl(providerResult.getCodeUrl());
                    result.setExpireTime(providerResult.getExpireTime());
                } else if (PayType.JSAPI.name().equals(payType)) {
                    // JSAPI支付: 保存prepayId
                    PaymentResult.WxPayParams wxParams = providerResult.getWxPayParams();
                    PayResult.WxPayParams params = new PayResult.WxPayParams();
                    params.setAppId(wxParams.getAppId());
                    params.setTimeStamp(wxParams.getTimeStamp());
                    params.setNonceStr(wxParams.getNonceStr());
                    params.setPackageValue(wxParams.getPackageValue());
                    params.setSignType(wxParams.getSignType());
                    params.setPaySign(wxParams.getPaySign());
                    result.setWxPayParams(params);
                }
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
     * 根据请求参数和配置确定支付类型
     * 优先级: 请求指定 > 配置mode
     */
    private String resolvePayType(String requestPayType) {
        // 如果请求指定了支付类型, 校验后使用
        if (requestPayType != null && !requestPayType.isEmpty()) {
            try {
                PayType.valueOf(requestPayType);
                // 生产环境禁止MOCK
                if (PayType.MOCK.name().equals(requestPayType) && !qgcWxPayConfig.isMockMode()) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "生产环境禁止Mock支付");
                }
                return requestPayType;
            } catch (IllegalArgumentException e) {
                log.warn("无效的支付类型: {}, 使用配置默认", requestPayType);
            }
        }
        // 使用配置的mode
        return qgcWxPayConfig.isMockMode() ? PayType.MOCK.name() : qgcWxPayConfig.getMode();
    }

    /**
     * 根据订单号查询支付订单（前端轮询用）
     */
    @Override
    public PaymentOrder getByOrderNo(String orderNo) {
        return paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getOrderNo, orderNo)
                        .last("LIMIT 1"));
    }

    /**
     * 微信支付回调处理
     * 幂等：transaction_id唯一索引 + 订单状态判断
     * 使用PaymentProvider解析通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePayNotify(String notifyData) {
        // Mock模式直接返回成功
        if (qgcWxPayConfig.isMockMode()) {
            return "SUCCESS";
        }

        // 记录回调日志
        PayNotifyLog notifyLog = new PayNotifyLog();
        notifyLog.setType("PAY");
        notifyLog.setRawData(notifyData.length() > 10000 ? notifyData.substring(0, 10000) : notifyData);

        try {
            // 使用PaymentProvider解析通知
            NotifyResult notifyResult = defaultProvider().handlePayNotify(notifyData);

            if (!"SUCCESS".equals(notifyResult.getResult())) {
                notifyLog.setProcessResult("FAIL");
                notifyLog.setErrorMsg(notifyResult.getErrorMsg());
                payNotifyLogMapper.insert(notifyLog);
                return "FAIL";
            }

            notifyLog.setOrderNo(notifyResult.getOutTradeNo());
            notifyLog.setTransactionId(notifyResult.getTransactionId());

            // 幂等检查：根据transaction_id查找支付订单
            PaymentOrder paymentOrder = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>()
                            .eq(PaymentOrder::getTransactionId, notifyResult.getTransactionId())
                            .last("LIMIT 1"));
            if (paymentOrder != null && PaymentStatus.SUCCESS.name().equals(paymentOrder.getStatus())) {
                // 已处理过，直接返回成功
                notifyLog.setProcessResult("SUCCESS");
                payNotifyLogMapper.insert(notifyLog);
                return "SUCCESS";
            }

            // 根据订单号查找
            paymentOrder = paymentOrderMapper.selectOne(
                    new LambdaQueryWrapper<PaymentOrder>()
                            .eq(PaymentOrder::getOrderNo, notifyResult.getOutTradeNo())
                            .last("LIMIT 1"));
            if (paymentOrder == null) {
                log.error("支付回调订单不存在: {}", notifyResult.getOutTradeNo());
                notifyLog.setProcessResult("FAIL");
                notifyLog.setErrorMsg("订单不存在: " + notifyResult.getOutTradeNo());
                payNotifyLogMapper.insert(notifyLog);
                return "FAIL";
            }

            // 幂等：状态必须是CREATED或PAYING
            if (!PaymentStatus.CREATED.name().equals(paymentOrder.getStatus())
                    && !PaymentStatus.PAYING.name().equals(paymentOrder.getStatus())) {
                notifyLog.setProcessResult("SUCCESS");
                payNotifyLogMapper.insert(notifyLog);
                return "SUCCESS";
            }

            // 金额校验：回调金额必须与订单金额一致
            Long paidAmount = notifyResult.getTotalAmount();
            if (!paidAmount.equals(paymentOrder.getAmount())) {
                log.error("支付回调金额不匹配: 订单金额={}, 回调金额={}", paymentOrder.getAmount(), paidAmount);
                notifyLog.setProcessResult("FAIL");
                notifyLog.setErrorMsg("金额不匹配");
                payNotifyLogMapper.insert(notifyLog);
                return "FAIL";
            }

            // 更新支付订单(DB条件更新: WHERE status IN ('CREATED','PAYING') 保证只有一个线程入账)
            LambdaUpdateWrapper<PaymentOrder> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PaymentOrder::getId, paymentOrder.getId())
                    .in(PaymentOrder::getStatus, PaymentStatus.CREATED.name(), PaymentStatus.PAYING.name())
                    .set(PaymentOrder::getStatus, PaymentStatus.SUCCESS.name())
                    .set(PaymentOrder::getEffectiveAmount, paidAmount)
                    .set(PaymentOrder::getTransactionId, notifyResult.getTransactionId())
                    .set(PaymentOrder::getPayTime, LocalDateTime.now())
                    .set(PaymentOrder::getNotifyTime, LocalDateTime.now());
            int rows = paymentOrderMapper.update(null, updateWrapper);
            if (rows == 0) {
                // 并发场景下其他线程已处理, 直接返回成功
                log.info("支付回调并发跳过: orderId={}", paymentOrder.getId());
                notifyLog.setProcessResult("SUCCESS");
                payNotifyLogMapper.insert(notifyLog);
                return "SUCCESS";
            }

            // 重新查询更新后的订单
            paymentOrder = paymentOrderMapper.selectById(paymentOrder.getId());

            // 处理支付成功
            processPaySuccess(paymentOrder);

            notifyLog.setProcessResult("SUCCESS");
            payNotifyLogMapper.insert(notifyLog);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("支付回调处理异常", e);
            notifyLog.setProcessResult("FAIL");
            notifyLog.setErrorMsg(e.getMessage());
            try { payNotifyLogMapper.insert(notifyLog); } catch (Exception ex) { log.error("记录回调日志失败", ex); }
            return "FAIL";
        }
    }

    /**
     * 微信退款回调处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleRefundNotify(String notifyData) {
        if (qgcWxPayConfig.isMockMode()) {
            return "SUCCESS";
        }

        // 记录回调日志
        PayNotifyLog notifyLog = new PayNotifyLog();
        notifyLog.setType("REFUND");
        notifyLog.setRawData(notifyData.length() > 10000 ? notifyData.substring(0, 10000) : notifyData);

        try {
            NotifyResult notifyResult = defaultProvider().handleRefundNotify(notifyData);

            if (!"SUCCESS".equals(notifyResult.getResult())) {
                notifyLog.setProcessResult("FAIL");
                notifyLog.setErrorMsg(notifyResult.getErrorMsg());
                payNotifyLogMapper.insert(notifyLog);
                return "FAIL";
            }

            notifyLog.setOrderNo(notifyResult.getOutRefundNo());
            notifyLog.setTransactionId(notifyResult.getRefundTransactionId());

            RefundOrder refundOrder = refundOrderMapper.selectOne(
                    new LambdaQueryWrapper<RefundOrder>()
                            .eq(RefundOrder::getRefundNo, notifyResult.getOutRefundNo())
                            .last("LIMIT 1"));
            if (refundOrder == null) {
                log.error("退款回调订单不存在: {}", notifyResult.getOutRefundNo());
                notifyLog.setProcessResult("FAIL");
                notifyLog.setErrorMsg("退款订单不存在");
                payNotifyLogMapper.insert(notifyLog);
                return "FAIL";
            }

            if ("SUCCESS".equals(notifyResult.getRefundStatus())) {
                refundOrder.setStatus(RefundStatus.SUCCESS.name());
                refundOrder.setWechatRefundId(notifyResult.getRefundTransactionId());
                refundOrder.setRefundTime(LocalDateTime.now());
            } else {
                refundOrder.setStatus(RefundStatus.FAIL.name());
            }
            refundOrderMapper.updateById(refundOrder);

            notifyLog.setProcessResult("SUCCESS");
            payNotifyLogMapper.insert(notifyLog);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("退款回调处理异常", e);
            notifyLog.setProcessResult("FAIL");
            notifyLog.setErrorMsg(e.getMessage());
            try { payNotifyLogMapper.insert(notifyLog); } catch (Exception ex) { log.error("记录回调日志失败", ex); }
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

        // 调用退款
        if (qgcWxPayConfig.isMockMode()) {
            refundOrder.setStatus(RefundStatus.SUCCESS.name());
            refundOrder.setRefundTime(LocalDateTime.now());
            refundOrderMapper.updateById(refundOrder);
            paymentOrder.setStatus(PaymentStatus.PART_REFUNDED.name());
            paymentOrderMapper.updateById(paymentOrder);
        } else {
            try {
                providerFor(paymentOrder.getPayType()).refund(
                        paymentOrder.getOrderNo(),
                        refundOrder.getRefundNo(),
                        refundAmount,
                        paymentOrder.getEffectiveAmount(),
                        reason
                );
                refundOrder.setStatus(RefundStatus.PROCESSING.name());
                refundOrderMapper.updateById(refundOrder);
            } catch (Exception e) {
                log.error("微信退款失败", e);
                refundOrder.setStatus(RefundStatus.FAIL.name());
                refundOrderMapper.updateById(refundOrder);
                paymentOrder.setStatus(PaymentStatus.REFUNDING.name());
                paymentOrderMapper.updateById(paymentOrder);
            }
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
        if (!qgcWxPayConfig.isMockMode()) {
            try {
                providerFor(paymentOrder.getPayType()).closePayment(paymentOrder.getOrderNo());
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

    private PaymentProvider providerFor(String payType) {
        return paymentProviders.stream()
                .filter(provider -> provider.getPayType().equals(payType))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_PARAMS_ERROR,
                        "支付方式未启用: " + payType));
    }

    private PaymentProvider defaultProvider() {
        return providerFor(qgcWxPayConfig.isMockMode() ? PayType.MOCK.name() : qgcWxPayConfig.getMode());
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
}
