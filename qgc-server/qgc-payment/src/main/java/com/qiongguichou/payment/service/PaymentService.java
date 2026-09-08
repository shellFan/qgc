package com.qiongguichou.payment.service;

import com.qiongguichou.payment.dto.PayRequest;
import com.qiongguichou.payment.dto.PayResult;
import com.qiongguichou.payment.entity.PaymentOrder;

import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 发起支付
     */
    PayResult pay(Long userId, String openid, PayRequest request);

    /**
     * 根据订单号查询支付订单（前端轮询用）
     */
    PaymentOrder getByOrderNo(String orderNo);

    /**
     * 微信支付回调处理
     */
    String handlePayNotify(String notifyData);

    /**
     * 微信退款回调处理
     */
    String handleRefundNotify(String notifyData);

    /**
     * 创建退款订单(超额自动退款)
     */
    void createRefundForOverpay(Long paymentOrderId, Long refundAmount, String reason);

    /**
     * 关闭超时未支付订单
     */
    void closeExpiredOrder(Long paymentOrderId);
}