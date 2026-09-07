package com.qiongguichou.controller;

import com.qiongguichou.core.config.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查和版本接口
 *
 * RC5增强：
 * - /api/health      综合健康检查(含DB/Redis状态)
 * - /api/health/live  Liveness探针(K8s/Docker存活检查)
 * - /api/health/ready Readiness探针(K8s/Docker就绪检查)
 * - /api/version      版本信息
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
     * 综合健康检查
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
     * Liveness探针 - 存活检查
     * GET /api/health/live
     *
     * 只要JVM在运行就返回UP，用于K8s/Docker判断是否需要重启容器
     */
    @GetMapping("/api/health/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(result);
    }

    /**
     * Readiness探针 - 就绪检查
     * GET /api/health/ready
     *
     * 检查所有关键依赖是否可用，用于K8s/Docker判断是否可以接收流量
     * DB和Redis都正常才返回UP，否则返回503
     */
    @GetMapping("/api/health/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().toString());

        boolean dbReady = false;
        boolean redisReady = false;

        // 检查数据库
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbReady = true;
        } catch (Exception e) {
            log.warn("就绪检查: 数据库不可用", e);
        }

        // 检查Redis
        try {
            redisService.get("qgc:health:check");
            redisReady = true;
        } catch (Exception e) {
            log.warn("就绪检查: Redis不可用", e);
        }

        Map<String, Object> components = new LinkedHashMap<>();
        components.put("database", dbReady ? "UP" : "DOWN");
        components.put("redis", redisReady ? "UP" : "DOWN");
        result.put("components", components);

        if (dbReady && redisReady) {
            result.put("status", "UP");
            return ResponseEntity.ok(result);
        } else {
            result.put("status", "DOWN");
            return ResponseEntity.status(503).body(result);
        }
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