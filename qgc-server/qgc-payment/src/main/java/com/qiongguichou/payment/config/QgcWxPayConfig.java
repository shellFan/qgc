package com.qiongguichou.payment.config;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信支付配置 - 支持MOCK/NATIVE/JSAPI三种模式
 *
 * 配置模型:
 * - qgc.pay.mode: MOCK(开发模拟)/NATIVE(扫码支付)/JSAPI(公众号支付)
 * - qgc.pay.merchant-mode: DIRECT(普通商户,默认)
 * - qgc.pay.native-expire-minutes: Native支付二维码过期时间(默认5分钟)
 *
 * ProductionGuard:
 * - NATIVE模式: 必须配置mchId/apiV3Key/privateKeyPath/certificateSerialNo/notifyUrl
 * - JSAPI模式: 额外必须配置appId
 * - MOCK模式: 生产环境禁止
 *
 * APIv3字段说明:
 * - apiV3Key: APIv3密钥(用于回调解密), 在微信商户平台设置
 * - privateKeyPath: 商户API私钥文件路径(apiclient_key.pem)
 * - certificateSerialNo: 商户API证书序列号
 * - certPath: 商户API证书文件路径(apiclient_cert.pem), 退款需要
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "qgc.pay")
public class QgcWxPayConfig {

    /** 支付模式: MOCK/NATIVE/JSAPI */
    private String mode = "MOCK";

    /** 商户模式: DIRECT(普通商户) */
    private String merchantMode = "DIRECT";

    /** Native支付二维码过期时间(分钟), 默认5分钟 */
    private int nativeExpireMinutes = 5;

    /** 微信支付AppId(JSAPI模式必须) */
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

    /** 是否Mock模式(兼容旧配置) */
    private boolean mockEnabled = true;

    /**
     * 判断当前是否Mock模式
     */
    public boolean isMockMode() {
        return "MOCK".equalsIgnoreCase(mode) || mockEnabled;
    }

    /**
     * 判断当前是否Native模式
     */
    public boolean isNativeMode() {
        return "NATIVE".equalsIgnoreCase(mode);
    }

    /**
     * 判断当前是否JSAPI模式
     */
    public boolean isJsapiMode() {
        return "JSAPI".equalsIgnoreCase(mode);
    }

    /**
     * WxPayService Bean - 仅在非Mock模式下创建
     * NATIVE和JSAPI模式都需要WxPayService
     */
    @Bean
    @ConditionalOnProperty(name = "qgc.pay.mode", havingValue = "NATIVE")
    public WxPayService wxPayServiceNative() {
        validatePayConfig();
        return buildWxPayService("NATIVE");
    }

    @Bean
    @ConditionalOnProperty(name = "qgc.pay.mode", havingValue = "JSAPI")
    public WxPayService wxPayServiceJsapi() {
        validatePayConfig();
        return buildWxPayService("JSAPI");
    }

    /**
     * 兼容旧配置: mock-enabled=false时也创建WxPayService
     */
    @Bean
    @ConditionalOnProperty(name = "qgc.pay.mock-enabled", havingValue = "false")
    public WxPayService wxPayServiceLegacy() {
        // 如果mode已经是NATIVE或JSAPI, 由上面的Bean创建, 这里不重复
        if (!isMockMode()) {
            return null; // 不会实际使用, 由Primary覆盖
        }
        log.warn("使用了旧配置qgc.pay.mock-enabled, 建议迁移到qgc.pay.mode");
        validatePayConfig();
        return buildWxPayService("JSAPI");
    }

    private WxPayService buildWxPayService(String tradeType) {
        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setMchKey(mchKey);
        payConfig.setApiV3Key(apiV3Key);
        payConfig.setPrivateKeyPath(privateKeyPath);
        payConfig.setCertSerialNo(certificateSerialNo);
        payConfig.setNotifyUrl(notifyUrl);
        payConfig.setKeyPath(keyPath);
        payConfig.setTradeType(tradeType);
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        log.info("WxPayService初始化完成: mode={}, tradeType={}, mchId={}", mode, tradeType, mchId);
        return wxPayService;
    }

    /**
     * 启动保护：真实支付模式下校验必填配置
     * NATIVE模式: mchId/apiV3Key/privateKeyPath/certificateSerialNo/notifyUrl
     * JSAPI模式: 额外需要appId
     */
    private void validatePayConfig() {
        StringBuilder missing = new StringBuilder();

        // 通用必填
        if (mchId == null || mchId.isEmpty()) missing.append(" QGC_PAY_MCH_ID(qgc.pay.mch-id)");
        if (apiV3Key == null || apiV3Key.isEmpty()) missing.append(" QGC_PAY_API_V3_KEY(qgc.pay.api-v3-key)");
        if (privateKeyPath == null || privateKeyPath.isEmpty()) missing.append(" QGC_PAY_PRIVATE_KEY_PATH(qgc.pay.private-key-path)");
        if (certificateSerialNo == null || certificateSerialNo.isEmpty()) missing.append(" QGC_PAY_CERTIFICATE_SERIAL_NO(qgc.pay.certificate-serial-no)");
        if (notifyUrl == null || notifyUrl.isEmpty()) missing.append(" QGC_PAY_NOTIFY_URL(qgc.pay.notify-url)");

        // JSAPI模式额外需要appId
        if (isJsapiMode()) {
            if (appId == null || appId.isEmpty()) missing.append(" QGC_PAY_APPID(qgc.pay.app-id, JSAPI模式必须)");
        }

        // Native模式不强制appId, 但如果有也设置
        if (isNativeMode()) {
            if (appId == null || appId.isEmpty()) {
                log.info("Native模式未配置appId, 不影响Native支付(无需公众号)");
            }
        }

        if (missing.length() > 0) {
            throw new IllegalStateException("Missing required payment config for " + mode + " mode:" + missing);
        }
    }
}