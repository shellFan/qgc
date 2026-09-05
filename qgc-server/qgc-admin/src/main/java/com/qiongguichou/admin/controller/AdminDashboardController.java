package com.qiongguichou.admin.controller;

import com.qiongguichou.admin.service.AdminDashboardService;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台-数据统计控制器
 */
@RestController
@RequestMapping("/admin/api/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 概览数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(adminDashboardService.getOverview());
    }

    /**
     * 趋势数据
     */
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend() {
        return Result.success(adminDashboardService.getTrend());
    }

    /**
     * 分类统计
     */
    @GetMapping("/category-stats")
    public Result<Map<String, Object>> categoryStats() {
        return Result.success(adminDashboardService.getCategoryStats());
    }

    /**
     * 待审核数量
     */
    @GetMapping("/pending-counts")
    public Result<Map<String, Object>> pendingCounts() {
        return Result.success(adminDashboardService.getPendingCounts());
    }
}