package com.qiongguichou.payment.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置
 * 仅在qgc.pay.mock-enabled=false时加载真实微信支付
 * 
 * APIv3字段说明:
 * - apiV3Key: APIv3密钥(用于回调解密), 在微信商户平台设置
 * - privateKeyPath: 商户API私钥文件路径(apiclient_key.pem)
 * - certificateSerialNo: 商户API证书序列号
 * - certPath: 商户API证书文件路径(apiclient_cert.pem), 退款需要
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qgc.pay")
public class QgcWxPayConfig {

    /** 微信支付AppId */
    private String appId;

    /** 微信支付商户号 */
    private String mchId;

    /** 微信支付商户密钥(APIv2, 兼容旧接口) */
    private String mchKey;

    /** APIv3密钥(用于回调通知解密) */
    private String apiV3Key;

    /** 商户API私钥文件路径(apiclient_key.pem) */
    private String privateKeyPath;

    /** 商户API证书序列号 */
    private String certificateSerialNo;

    /** 商户API证书文件路径(apiclient_cert.pem, 退款需要) */
    private String certPath;

    /** 支付回调地址 */
    private String notifyUrl;

    /** 退款回调地址 */
    private String refundNotifyUrl;

    /** 证书路径(退款需要, 兼容APIv2) */
    private String keyPath;

    /** 是否Mock模式 */
    private boolean mockEnabled = true;

    @Bean
    @ConditionalOnProperty(name = "qgc.pay.mock-enabled", havingValue = "false", matchIfMissing = false)
    public WxPayService wxPayService() {
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setMchKey(mchKey);
        payConfig.setApiV3Key(apiV3Key);
        payConfig.setPrivateKeyPath(privateKeyPath);
        payConfig.setCertSerialNo(certificateSerialNo);
        payConfig.setNotifyUrl(notifyUrl);
        payConfig.setKeyPath(keyPath);
        payConfig.setTradeType("JSAPI");
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }
}