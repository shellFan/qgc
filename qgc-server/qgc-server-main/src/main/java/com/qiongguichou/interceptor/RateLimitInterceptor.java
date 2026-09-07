package com.qiongguichou.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.core.config.RedisService;
import com.qiongguichou.core.entity.SystemConfig;
import com.qiongguichou.core.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 风控限流拦截器
 * 从system_config读取ratelimit.*配置，使用Redis计数限流
 * 配置格式: ratelimit.{action} = {maxCount}/{timeWindowSeconds}
 * 例如: ratelimit.create_campaign = 5/3600 表示创建筹款每小时最多5次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final SystemConfigMapper systemConfigMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    /** 限流配置缓存(内存)，定期从DB刷新 */
    private final Map<String, RateLimitRule> ruleCache = new ConcurrentHashMap<>();
    /** 配置缓存过期时间(秒) */
    private volatile long cacheExpireAt = 0;
    /** 配置缓存有效期(30秒) */
    private static final long CACHE_TTL_MS = 30_000;

    /** 限流规则 */
    private static class RateLimitRule {
        final int maxCount;
        final int timeWindowSeconds;
        RateLimitRule(int maxCount, int timeWindowSeconds) {
            this.maxCount = maxCount;
            this.timeWindowSeconds = timeWindowSeconds;
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 根据URI匹配限流规则
        String action = matchAction(request.getRequestURI(), request.getMethod());
        if (action == null) {
            return true; // 无匹配规则，放行
        }

        // 获取限流规则
        RateLimitRule rule = getRule(action);
        if (rule == null) {
            return true; // 无配置，放行
        }

        // 构建限流key: qgc:ratelimit:{action}:{userId或IP}
        String identity = getUserIdentity(request);
        String limitKey = "qgc:ratelimit:" + action + ":" + identity;

        // Redis计数限流
        String countStr = redisService.get(limitKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        if (count >= rule.maxCount) {
            writeTooManyRequests(response);
            return false;
        }

        // 增加计数
        if (count == 0) {
            redisService.set(limitKey, "1", rule.timeWindowSeconds, TimeUnit.SECONDS);
        } else {
            redisService.increment(limitKey);
        }

        return true;
    }

    /**
     * 根据URI匹配限流动作
     */
    private String matchAction(String uri, String method) {
        if (uri.startsWith("/api/campaign") && "POST".equalsIgnoreCase(method)) {
            return "create_campaign";
        }
        if (uri.startsWith("/api/pay") && "POST".equalsIgnoreCase(method)) {
            return "support";
        }
        if (uri.startsWith("/api/comment") && "POST".equalsIgnoreCase(method)) {
            return "comment";
        }
        if (uri.startsWith("/api/like") && "POST".equalsIgnoreCase(method)) {
            return "like";
        }
        if (uri.startsWith("/api/share") && "POST".equalsIgnoreCase(method)) {
            return "share";
        }
        return null;
    }

    /**
     * 获取限流规则(带内存缓存，30秒刷新)
     */
    private RateLimitRule getRule(String action) {
        long now = System.currentTimeMillis();
        if (cacheExpireAt == 0 || now > cacheExpireAt) {
            refreshRuleCache();
            cacheExpireAt = now + CACHE_TTL_MS;
        }
        return ruleCache.get(action);
    }

    /**
     * 从DB刷新限流规则缓存
     */
    private void refreshRuleCache() {
        try {
            List<SystemConfig> configs = systemConfigMapper.selectList(
                    new LambdaQueryWrapper<SystemConfig>().likeRight(SystemConfig::getConfigKey, "ratelimit."));
            Map<String, RateLimitRule> newCache = new ConcurrentHashMap<>();
            for (SystemConfig config : configs) {
                String action = config.getConfigKey().substring("ratelimit.".length());
                RateLimitRule rule = parseRule(config.getConfigValue());
                if (rule != null) {
                    newCache.put(action, rule);
                }
            }
            ruleCache.clear();
            ruleCache.putAll(newCache);
        } catch (Exception e) {
            log.warn("刷新限流规则缓存失败", e);
        }
    }

    /**
     * 解析限流规则 "5/3600" → RateLimitRule(5, 3600)
     */
    private RateLimitRule parseRule(String value) {
        try {
            String[] parts = value.split("/");
            return new RateLimitRule(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (Exception e) {
            log.warn("解析限流规则失败: {}", value);
            return null;
        }
    }

    /**
     * 获取用户标识(userId或IP)
     */
    private String getUserIdentity(HttpServletRequest request) {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            return "u:" + userId;
        }
        String ip = request.getRemoteAddr();
        return "ip:" + (ip != null ? ip : "unknown");
    }

    /**
     * 写429响应
     */
    private void writeTooManyRequests(HttpServletResponse response) throws Exception {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(ErrorCode.TOO_MANY_REQUESTS);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}