package com.qiongguichou.payment.provider;

import lombok.Data;

/**
 * PaymentProvider回调通知处理结果
 */
@Data
public class NotifyResult {

    /** 处理结果: SUCCESS/FAIL */
    private String result;

    /** 商户订单号 */
    private String outTradeNo;

    /** 微信支付交易号 */
    private String transactionId;

    /** 支付金额(分) */
    private Long totalAmount;

    /** 退款单号(退款回调) */
    private String outRefundNo;

    /** 退款状态(退款回调): SUCCESS/FAIL/CHANGE */
    private String refundStatus;

    /** 微信退款单号(退款回调) */
    private String refundTransactionId;

    /** 错误信息 */
    private String errorMsg;

    public static NotifyResult success() {
        NotifyResult r = new NotifyResult();
        r.setResult("SUCCESS");
        return r;
    }

    public static NotifyResult fail(String errorMsg) {
        NotifyResult r = new NotifyResult();
        r.setResult("FAIL");
        r.setErrorMsg(errorMsg);
        return r;
    }
}