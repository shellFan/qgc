package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 后台-审计日志Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<AdminLog> page(int pageNum, int pageSize, String adminUsername, String action, String targetType) {
        Page<AdminLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdminLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(adminUsername)) {
            wrapper.like(AdminLog::getAdminUsername, adminUsername);
        }
        if (StringUtils.hasText(action)) {
            wrapper.like(AdminLog::getAction, action);
        }
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(AdminLog::getTargetType, targetType);
        }
        wrapper.orderByDesc(AdminLog::getCreateTime);
        return adminLogMapper.selectPage(page, wrapper);
    }

    @Override
    public AdminLog getDetail(Long id) {
        return adminLogMapper.selectById(id);
    }
}