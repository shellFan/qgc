package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminPaymentService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.entity.RefundOrder;
import com.qiongguichou.payment.entity.SupportOrder;
import com.qiongguichou.payment.mapper.PaymentOrderMapper;
import com.qiongguichou.payment.mapper.RefundOrderMapper;
import com.qiongguichou.payment.mapper.SupportOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台-支付管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentOrderMapper paymentOrderMapper;
    private final SupportOrderMapper supportOrderMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<PaymentOrder> listPayments(Page<PaymentOrder> page, String status, Long campaignId, String orderNo) {
        LambdaQueryWrapper<PaymentOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PaymentOrder::getStatus, status);
        }
        if (campaignId != null) {
            wrapper.eq(PaymentOrder::getCampaignId, campaignId);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(PaymentOrder::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(PaymentOrder::getCreateTime);
        return paymentOrderMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getPaymentDetail(Long id) {
        PaymentOrder payment = paymentOrderMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付订单不存在");
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("payment", payment);

        // 查询关联支持订单
        if (payment.getSupportNo() != null) {
            SupportOrder support = supportOrderMapper.selectOne(
                    new LambdaQueryWrapper<SupportOrder>()
                            .eq(SupportOrder::getSupportNo, payment.getSupportNo()));
            detail.put("support", support);
        }

        // 查询关联退款订单
        detail.put("refunds", refundOrderMapper.selectList(
                new LambdaQueryWrapper<RefundOrder>()
                        .eq(RefundOrder::getPaymentOrderNo, payment.getOrderNo())
                        .orderByDesc(RefundOrder::getCreateTime)));

        return detail;
    }

    @Override
    @Transactional
    public void refund(Long id, Long amount, String reason, Long adminId) {
        PaymentOrder payment = paymentOrderMapper.selectById(id);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付订单不存在");
        }
        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支付成功的订单可退款");
        }

        // 创建退款记录
        RefundOrder refund = new RefundOrder();
        refund.setPaymentOrderNo(payment.getOrderNo());
        refund.setCampaignId(payment.getCampaignId());
        refund.setUserId(payment.getSupporterUserId());
        refund.setAmount(amount);
        refund.setReason(reason);
        refund.setType("ADMIN");
        refund.setStatus("PENDING");
        refundOrderMapper.insert(refund);

        // 记录管理员操作
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("REFUND");
        log.setTargetType("PAYMENT");
        log.setTargetId(String.valueOf(id));
        log.setAfterData("refundAmount=" + amount + ",reason=" + reason);
        adminLogMapper.insert(log);
    }
}