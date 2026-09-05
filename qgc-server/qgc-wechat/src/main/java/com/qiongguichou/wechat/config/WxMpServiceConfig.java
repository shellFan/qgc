package com.qiongguichou.wechat.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信公众号Service Bean配置
 * 仅在非Mock模式创建
 */
@Configuration
@ConditionalOnProperty(name = "qgc.wechat.mock-enabled", havingValue = "false", matchIfMissing = false)
public class WxMpServiceConfig {

    @Bean
    public WxMpService wxMpService(WxMpConfig wxMpConfig) {
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(wxMpConfig.getAppId());
        config.setSecret(wxMpConfig.getAppSecret());
        config.setToken(wxMpConfig.getToken());
        config.setAesKey(wxMpConfig.getAesKey());

        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(config);
        return service;
    }
}