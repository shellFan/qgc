package com.qiongguichou.payment.dto;

import lombok.Data;

/**
 * 支付结果 - 返回给前端的支付参数
 * 支持三种模式:
 * - MOCK: mock=true, 无需支付参数
 * - NATIVE: payType=NATIVE, codeUrl+expireTime, 前端生成二维码
 * - JSAPI: payType=JSAPI, wxPayParams, 微信内H5拉起支付
 */
@Data
public class PayResult {

    /** 支持订单号 */
    private String orderNo;

    /** 支付订单号 */
    private String paymentOrderNo;

    /** 支付类型: MOCK/NATIVE/JSAPI */
    private String payType;

    /** 是否Mock模式 */
    private boolean mock;

    /** Native支付二维码URL(weixin://wxpay/...) */
    private String codeUrl;

    /** Native支付过期时间(ISO8601格式) */
    private String expireTime;

    /** 微信JSAPI支付参数(JSAPI模式下才有) */
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