package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.config.RedisService;
import com.qiongguichou.core.entity.RiskRecord;
import com.qiongguichou.core.entity.SystemConfig;
import com.qiongguichou.core.mapper.RiskRecordMapper;
import com.qiongguichou.core.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 后台-风控配置Controller
 */
@RestController
@RequestMapping("/admin/api/risk")
@RequiredArgsConstructor
public class AdminRiskController {

    private final RiskRecordMapper riskRecordMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final RedisService redisService;

    /** 限流配置Redis缓存key前缀 */
    private static final String RATELIMIT_CACHE_KEY = "qgc:config:ratelimit";

    /**
     * 获取风控记录列表
     */
    @GetMapping("/records")
    public Result<IPage<RiskRecord>> getRiskRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        Page<RiskRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<RiskRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(RiskRecord::getStatus, status);
        }
        wrapper.orderByDesc(RiskRecord::getCreateTime);
        return Result.success(riskRecordMapper.selectPage(page, wrapper));
    }

    /**
     * 处理风控记录
     */
    @PostMapping("/record/{id}")
    public Result<Void> handleRiskRecord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        RiskRecord record = riskRecordMapper.selectById(id);
        if (record != null) {
            record.setStatus((String) body.get("status"));
            record.setHandleRemark((String) body.get("remark"));
            riskRecordMapper.updateById(record);
        }
        return Result.success();
    }

    /**
     * 获取风控规则配置
     */
    @GetMapping("/config")
    public Result<Map<String, String>> getRiskConfig() {
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().likeRight(SystemConfig::getConfigKey, "ratelimit."));
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return Result.success(result);
    }

    /**
     * 更新风控规则配置(更新后清除Redis缓存使限流拦截器重新加载)
     */
    @PostMapping("/config")
    public Result<Void> updateRiskConfig(@RequestBody Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfig config = systemConfigMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, entry.getKey()));
            if (config != null) {
                config.setConfigValue(entry.getValue());
                systemConfigMapper.updateById(config);
            }
        }
        // 清除限流缓存，使RateLimitInterceptor重新从DB加载
        try {
            redisService.delete(RATELIMIT_CACHE_KEY);
            // 同时清除所有已有的限流计数key(配置变更后重置计数)
            Set<String> limitKeys = redisService.getKeysByPattern("qgc:ratelimit:*");
            if (limitKeys != null) {
                for (String key : limitKeys) {
                    redisService.delete(key);
                }
            }
        } catch (Exception e) {
            // 缓存清除失败不影响配置更新
        }
        return Result.success();
    }
}