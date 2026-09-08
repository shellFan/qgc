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
 * 确保prod/staging环境下配置安全
 *
 * 语义区分：
 * - staging: 安全项必须严格(JWT/CORS)，但微信/支付mock允许(CONFIG REQUIRED)
 * - prod: 所有mock必须禁止，Redis必须有密码
 */
@Slf4j
@Component
public class ProdSecurityCheck {

    private static final String DEFAULT_JWT_SECRET = "qiongguichou2026secretkey";

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
        List<String> warnings = new ArrayList<>();

        // 1. JWT密钥检查 - staging和prod都必须
        String jwtSecret = env.getProperty("qgc.jwt.secret", "");
        if (jwtSecret.isEmpty()) {
            violations.add("qgc.jwt.secret 为空 (必须设置JWT密钥)");
        } else if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            violations.add("qgc.jwt.secret 使用默认值 (必须更换为强密钥)");
        }

        // 2. CORS检查 - staging和prod都必须
        String corsOrigins = env.getProperty("cors.allowed-origins", "");
        if (corsOrigins.isEmpty()) {
            violations.add("cors.allowed-origins 为空 (必须配置具体域名)");
        } else if (corsOrigins.contains("*")) {
            violations.add("cors.allowed-origins 包含通配符* (禁止使用通配符)");
        }

        // 3. Mock模式检查 - 区分staging和prod
        boolean authMock = Boolean.parseBoolean(env.getProperty("qgc.auth.mock-enabled", "false"));
        boolean wechatMock = Boolean.parseBoolean(env.getProperty("qgc.wechat.mock-enabled", "false"));
        boolean payMock = Boolean.parseBoolean(env.getProperty("qgc.pay.mock-enabled", "false"));

        if (isProd) {
            // 生产环境：所有mock必须禁止
            if (authMock) {
                violations.add("qgc.auth.mock-enabled = true (生产环境禁止Mock登录)");
            }
            if (wechatMock) {
                violations.add("qgc.wechat.mock-enabled = true (生产环境禁止Mock微信)");
            }
            if (payMock) {
                violations.add("qgc.pay.mock-enabled = true (生产环境禁止Mock支付)");
            }
        } else {
            // Staging环境：auth mock禁止，wechat/pay mock允许但标记CONFIG REQUIRED
            if (authMock) {
                violations.add("qgc.auth.mock-enabled = true (Staging环境禁止Mock登录)");
            }
            if (wechatMock) {
                warnings.add("qgc.wechat.mock-enabled = true (CONFIG REQUIRED: 需要真实微信公众号配置才能关闭mock)");
            }
            if (payMock) {
                warnings.add("qgc.pay.mock-enabled = true (CONFIG REQUIRED: 需要真实微信支付配置才能关闭mock)");
            }
        }

        // 4. 生产环境额外检查
        if (isProd) {
            String redisPassword = env.getProperty("spring.redis.password", "");
            if (redisPassword.isEmpty()) {
                violations.add("spring.redis.password 为空 (生产环境Redis必须设置密码)");
            }
        }

        // 输出警告(CONFIG REQUIRED但允许启动)
        if (!warnings.isEmpty()) {
            log.warn("============================================");
            log.warn("【配置提醒】{}环境以下配置需要真实凭据：", envName);
            for (String w : warnings) {
                log.warn("  ⚠️ {}", w);
            }
            log.warn("============================================");
        }

        // 输出违规(必须修复才能启动)
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

        log.info("✅ {}环境安全检查通过{}", envName, warnings.isEmpty() ? "" : " (有配置提醒，请关注上方警告)");
    }
}