package com.qiongguichou.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务封装
 */
@Slf4j
@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 自增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 自增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 限流检查
     * @param key 限流key
     * @param maxCount 最大次数
     * @param timeWindow 时间窗口(秒)
     * @return 是否允许通过
     */
    public boolean rateLimit(String key, int maxCount, int timeWindow) {
        Long count = increment(key);
        if (count != null && count == 1) {
            expire(key, timeWindow, TimeUnit.SECONDS);
        }
        return count == null || count <= maxCount;
    }

    /**
     * 缓存浏览量+1
     */
    public void incrementViewCount(Long campaignId) {
        String key = "qgc:campaign:views:" + campaignId;
        increment(key);
    }

    /**
     * 获取缓存浏览量
     */
    public Integer getViewCount(Long campaignId) {
        String key = "qgc:campaign:views:" + campaignId;
        String value = get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 清除浏览量缓存
     */
    public void clearViewCount(Long campaignId) {
        String key = "qgc:campaign:views:" + campaignId;
        delete(key);
    }

    /**
     * 按模式获取keys
     */
    public Set<String> getKeysByPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }
}