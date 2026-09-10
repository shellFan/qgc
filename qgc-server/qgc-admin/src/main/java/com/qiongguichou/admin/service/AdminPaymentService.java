package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.payment.entity.PaymentOrder;

import java.util.Map;

/**
 * 后台-支付管理Service
 */
public interface AdminPaymentService {

    /**
     * 分页查询支付订单
     */
    IPage<PaymentOrder> listPayments(Page<PaymentOrder> page, String status, Long campaignId, String orderNo);

    /**
     * 获取支付订单详情(含支持订单)
     */
    Map<String, Object> getPaymentDetail(Long id);

    /**
     * 发起退款
     */
    void refund(Long id, Long amount, String reason, Long adminId);
}