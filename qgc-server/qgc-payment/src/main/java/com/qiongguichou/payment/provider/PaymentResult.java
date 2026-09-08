package com.qiongguichou.payment.provider;

import lombok.Data;

/**
 * PaymentProvider创建支付返回结果
 */
@Data
public class PaymentResult {

    /** 支付类型: MOCK/NATIVE/JSAPI */
    private String payType;

    /** 是否Mock模式 */
    private boolean mock;

    /** Native支付二维码URL(weixin://wxpay/...) */
    private String codeUrl;

    /** Native支付过期时间(ISO8601) */
    private String expireTime;

    /** JSAPI支付参数 */
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

    public static PaymentResult mock() {
        PaymentResult r = new PaymentResult();
        r.setPayType("MOCK");
        r.setMock(true);
        return r;
    }

    public static PaymentResult nativePay(String codeUrl, String expireTime) {
        PaymentResult r = new PaymentResult();
        r.setPayType("NATIVE");
        r.setMock(false);
        r.setCodeUrl(codeUrl);
        r.setExpireTime(expireTime);
        return r;
    }

    public static PaymentResult jsapiPay(WxPayParams params) {
        PaymentResult r = new PaymentResult();
        r.setPayType("JSAPI");
        r.setMock(false);
        r.setWxPayParams(params);
        return r;
    }
}