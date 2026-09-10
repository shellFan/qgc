package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.admin.entity.AdminLog;

/**
 * 后台-审计日志Service
 */
public interface AdminAuditLogService {

    /**
     * 分页查询审计日志
     */
    IPage<AdminLog> page(int pageNum, int pageSize, String adminUsername, String action, String targetType);

    /**
     * 获取日志详情
     */
    AdminLog getDetail(Long id);
}