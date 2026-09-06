package com.qiongguichou.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 生产环境安全检查
 * 确保prod环境下不能开启Mock模式
 */
@Slf4j
@Component
public class ProdSecurityCheck {

    private final Environment env;

    public ProdSecurityCheck(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkMockSecurity() {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
        if (!isProd) {
            return;
        }

        boolean authMock = Boolean.parseBoolean(env.getProperty("qgc.auth.mock-enabled", "false"));
        boolean wechatMock = Boolean.parseBoolean(env.getProperty("qgc.wechat.mock-enabled", "false"));
        boolean payMock = Boolean.parseBoolean(env.getProperty("qgc.pay.mock-enabled", "false"));

        if (authMock || wechatMock || payMock) {
            log.error("============================================");
            log.error("【安全警告】生产环境检测到Mock模式开启！");
            if (authMock) {
                log.error("  - qgc.auth.mock-enabled = true (禁止Mock登录)");
            }
            if (wechatMock) {
                log.error("  - qgc.wechat.mock-enabled = true (禁止Mock微信)");
            }
            if (payMock) {
                log.error("  - qgc.pay.mock-enabled = true (禁止Mock支付)");
            }
            log.error("生产环境严禁开启Mock模式，应用即将关闭！");
            log.error("============================================");
            System.exit(1);
        }

        log.info("生产环境安全检查通过：Mock模式已关闭");
    }
}