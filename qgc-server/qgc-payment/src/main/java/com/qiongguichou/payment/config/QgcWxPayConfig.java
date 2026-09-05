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
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qgc.pay")
public class QgcWxPayConfig {

    /** 微信支付AppId */
    private String appId;

    /** 微信支付商户号 */
    private String mchId;

    /** 微信支付商户密钥 */
    private String mchKey;

    /** 支付回调地址 */
    private String notifyUrl;

    /** 退款回调地址 */
    private String refundNotifyUrl;

    /** 证书路径(退款需要) */
    private String keyPath;

    /** 是否Mock模式 */
    private boolean mockEnabled = true;

    @Bean
    @ConditionalOnProperty(name = "qgc.pay.mock-enabled", havingValue = "false", matchIfMissing = false)
    public WxPayService wxPayService() {
        com.github.binarywang.wxpay.config.WxPayConfig payConfig = new com.github.binarywang.wxpay.config.WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setMchKey(mchKey);
        payConfig.setNotifyUrl(notifyUrl);
        payConfig.setKeyPath(keyPath);
        payConfig.setTradeType("JSAPI");
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }
}