package com.qiongguichou.wechat.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信公众号配置
 * 开发环境可设置 qgc.wechat.mock-enabled=true 跳过真实微信API
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qgc.wechat.mp")
@ConditionalOnProperty(name = "qgc.wechat.mock-enabled", havingValue = "false", matchIfMissing = false)
public class WxMpConfig {

    /** 公众号AppId */
    private String appId;

    /** 公众号AppSecret */
    private String appSecret;

    /** 消息Token */
    private String token;

    /** 消息EncodingAESKey */
    private String aesKey;
}