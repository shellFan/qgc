package com.qiongguichou.payment.provider.impl;

import com.qiongguichou.common.enums.PayType;
import com.qiongguichou.payment.entity.PaymentOrder;
import com.qiongguichou.payment.provider.NotifyResult;
import com.qiongguichou.payment.provider.PaymentProvider;
import com.qiongguichou.payment.provider.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock支付提供者 - 开发环境模拟支付
 * 所有操作直接返回成功，不调用真实微信API
 * 仅在qgc.pay.mode=MOCK或未配置时加载
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "qgc.pay.mode", havingValue = "MOCK", matchIfMissing = true)
public class MockWechatPaymentProvider implements PaymentProvider {

    @Override
    public PaymentResult createPayment(PaymentOrder paymentOrder, String openid) {
        log.info("[Mock] 创建支付: orderNo={}, amount={}", paymentOrder.getOrderNo(), paymentOrder.getAmount());
        return PaymentResult.mock();
    }

    @Override
    public String queryPayment(String orderNo) {
        log.info("[Mock] 查询支付: orderNo={}", orderNo);
        return "SUCCESS";
    }

    @Override
    public void closePayment(String orderNo) {
        log.info("[Mock] 关闭订单: orderNo={}", orderNo);
    }

    @Override
    public void refund(String orderNo, String refundNo, Long refundAmount, Long totalAmount, String reason) {
        log.info("[Mock] 退款: orderNo={}, refundNo={}, amount={}", orderNo, refundNo, refundAmount);
    }

    @Override
    public NotifyResult handlePayNotify(String notifyData) {
        log.info("[Mock] 支付回调处理(跳过)");
        return NotifyResult.success();
    }

    @Override
    public NotifyResult handleRefundNotify(String notifyData) {
        log.info("[Mock] 退款回调处理(跳过)");
        return NotifyResult.success();
    }

    @Override
    public String getPayType() {
        return PayType.MOCK.name();
    }

    @Override
    public boolean isMock() {
        return true;
    }
}