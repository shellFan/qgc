package com.qiongguichou.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产环境安全检查 (Production Guard)
 * 确保prod/staging环境下配置安全，不满足条件则拒绝启动
 *
 * RC5增强：新增JWT密钥检查、CORS检查
 */
@Slf4j
@Component
public class ProdSecurityCheck {

    private static final String DEFAULT_JWT_SECRET = "qiongguichou2026secretkey_dev_only";

    private final Environment env;

    public ProdSecurityCheck(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkProductionSecurity() {
        boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
        boolean isStaging = Arrays.asList(env.getActiveProfiles()).contains("staging");

        if (!isProd && !isStaging) {
            log.info("开发环境，跳过生产安全检查");
            return;
        }

        String envName = isProd ? "生产" : "Staging";
        List<String> violations = new ArrayList<>();

        // 1. Mock模式检查
        boolean authMock = Boolean.parseBoolean(env.getProperty("qgc.auth.mock-enabled", "false"));
        boolean wechatMock = Boolean.parseBoolean(env.getProperty("qgc.wechat.mock-enabled", "false"));
        boolean payMock = Boolean.parseBoolean(env.getProperty("qgc.pay.mock-enabled", "false"));

        if (authMock) {
            violations.add("qgc.auth.mock-enabled = true (禁止Mock登录)");
        }
        if (wechatMock) {
            violations.add("qgc.wechat.mock-enabled = true (禁止Mock微信)");
        }
        if (payMock) {
            violations.add("qgc.pay.mock-enabled = true (禁止Mock支付)");
        }

        // 2. JWT密钥检查
        String jwtSecret = env.getProperty("qgc.jwt.secret", "");
        if (jwtSecret.isEmpty()) {
            violations.add("qgc.jwt.secret 为空 (必须设置JWT密钥)");
        } else if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            violations.add("qgc.jwt.secret 使用默认值 (必须更换为强密钥)");
        }

        // 3. CORS检查
        String corsOrigins = env.getProperty("cors.allowed-origins", "");
        if (corsOrigins.isEmpty()) {
            violations.add("cors.allowed-origins 为空 (必须配置具体域名)");
        } else if (corsOrigins.contains("*")) {
            violations.add("cors.allowed-origins 包含通配符* (禁止使用通配符)");
        }

        // 4. 生产环境额外检查
        if (isProd) {
            String redisPassword = env.getProperty("spring.redis.password", "");
            if (redisPassword.isEmpty()) {
                violations.add("spring.redis.password 为空 (生产环境Redis必须设置密码)");
            }
        }

        if (!violations.isEmpty()) {
            log.error("============================================");
            log.error("【安全警告】{}环境安全检查未通过！", envName);
            for (String v : violations) {
                log.error("  ❌ {}", v);
            }
            log.error("{}环境配置不合规，应用即将关闭！", envName);
            log.error("============================================");
            System.exit(1);
        }

        log.info("✅ {}环境安全检查通过", envName);
    }
}