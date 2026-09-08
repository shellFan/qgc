package com.qiongguichou.payment.provider;

import com.qiongguichou.payment.entity.PaymentOrder;

/**
 * 支付提供者接口 - 统一支付抽象
 * 实现类: MockWechatPaymentProvider / NativeWechatPaymentProvider / JsapiWechatPaymentProvider
 *
 * 核心原则:
 * - 金额单位: Long(分), 禁止BigDecimal/float
 * - 普通商户DIRECT: 禁止sp_mchid/sub_mchid
 * - Native不需要openid/AppId
 * - JSAPI需要openid/AppId
 */
public interface PaymentProvider {

    /**
     * 创建支付(统一下单)
     * @param paymentOrder 支付订单(已创建,含orderNo/amount等)
     * @param openid 用户openid(Native模式可为null)
     * @return 支付结果(含codeUrl或wxPayParams)
     */
    PaymentResult createPayment(PaymentOrder paymentOrder, String openid);

    /**
     * 查询支付状态
     * @param orderNo 商户订单号
     * @return 微信返回的支付状态(SUCCESS/NOTPAY/CLOSED等)
     */
    String queryPayment(String orderNo);

    /**
     * 关闭订单
     * @param orderNo 商户订单号
     */
    void closePayment(String orderNo);

    /**
     * 退款
     * @param orderNo 商户订单号
     * @param refundNo 退款单号
     * @param refundAmount 退款金额(分)
     * @param totalAmount 原订单金额(分)
     * @param reason 退款原因
     */
    void refund(String orderNo, String refundNo, Long refundAmount, Long totalAmount, String reason);

    /**
     * 处理支付回调通知
     * @param notifyData 回调原始数据
     * @return 处理结果(SUCCESS/FAIL)
     */
    NotifyResult handlePayNotify(String notifyData);

    /**
     * 处理退款回调通知
     * @param notifyData 回调原始数据
     * @return 处理结果
     */
    NotifyResult handleRefundNotify(String notifyData);

    /**
     * 支付提供者类型
     */
    String getPayType();

    /**
     * 是否Mock模式
     */
    boolean isMock();
}