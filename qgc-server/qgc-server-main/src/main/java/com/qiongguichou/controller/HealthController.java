package com.qiongguichou.controller;

import com.qiongguichou.core.config.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查和版本接口
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisService redisService;

    @Value("${qgc.version:dev}")
    private String appVersion;

    /**
     * 健康检查接口
     * GET /api/health
     */
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("version", appVersion);

        // 检查数据库连接
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("database", "UP");
        } catch (Exception e) {
            result.put("database", "DOWN");
            result.put("status", "DEGRADED");
            log.warn("健康检查: 数据库连接异常", e);
        }

        // 检查Redis连接
        try {
            redisService.get("qgc:health:check");
            result.put("redis", "UP");
        } catch (Exception e) {
            result.put("redis", "DOWN");
            result.put("status", "DEGRADED");
            log.warn("健康检查: Redis连接异常", e);
        }

        return result;
    }

    /**
     * 版本接口
     * GET /api/version
     */
    @GetMapping("/api/version")
    public Map<String, String> version() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("version", appVersion);
        result.put("timestamp", LocalDateTime.now().toString());
        return result;
    }
}