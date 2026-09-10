package com.qiongguichou.admin.service;

import com.qiongguichou.core.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 后台-系统配置Service
 */
public interface AdminSystemConfigService {

    /**
     * 获取所有配置
     */
    List<SystemConfig> listAll();

    /**
     * 按分组前缀获取配置
     */
    Map<String, String> listByGroup(String group);

    /**
     * 更新配置值
     */
    void updateConfig(String key, String value, Long adminId);
}