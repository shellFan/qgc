package com.qiongguichou.payment.dto;

import lombok.Data;

/**
 * 支付结果 - 返回给前端的微信支付参数
 */
@Data
public class PayResult {

    /** 支持订单号 */
    private String orderNo;

    /** 支付订单号 */
    private String paymentOrderNo;

    /** 微信JSAPI支付参数 */
    private WxPayParams wxPayParams;

    @Data
    public static class WxPayParams {
        private String appId;
        private String timeStamp;
        private String nonceStr;
        private String packageValue;
        private String signType;
        private String paySign;
    }
}