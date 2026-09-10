package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminSystemConfigService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.core.entity.SystemConfig;
import com.qiongguichou.core.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台-系统配置Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminSystemConfigServiceImpl implements AdminSystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public List<SystemConfig> listAll() {
        return systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getConfigKey));
    }

    @Override
    public Map<String, String> listByGroup(String group) {
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>()
                        .likeRight(SystemConfig::getConfigKey, group + ".")
                        .orderByAsc(SystemConfig::getConfigKey));
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    public void updateConfig(String key, String value, Long adminId) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key));
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "配置项不存在: " + key);
        }
        String oldValue = config.getConfigValue();
        config.setConfigValue(value);
        systemConfigMapper.updateById(config);

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("UPDATE_CONFIG");
        log.setTargetType("SYSTEM_CONFIG");
        log.setTargetId(key);
        log.setBeforeData(oldValue);
        log.setAfterData(value);
        adminLogMapper.insert(log);
    }
}