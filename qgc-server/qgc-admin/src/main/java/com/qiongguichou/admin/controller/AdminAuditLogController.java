package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.service.AdminAuditLogService;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台-审计日志Controller
 */
@RestController
@RequestMapping("/admin/api/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    /**
     * 分页查询审计日志
     */
    @GetMapping
    public Result<IPage<AdminLog>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String adminUsername,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType) {
        return Result.success(adminAuditLogService.page(pageNum, pageSize, adminUsername, action, targetType));
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    public Result<AdminLog> detail(@PathVariable Long id) {
        return Result.success(adminAuditLogService.getDetail(id));
    }
}