package com.qiongguichou.admin.service;

import java.util.Map;

/**
 * 后台-数据统计服务
 */
public interface AdminDashboardService {

    /**
     * 获取概览数据
     * 返回：totalUsers, totalCampaigns, activeCampaigns, totalSupports,
     *       totalAmount(分), todayUsers, todaySupports, todayAmount(分)
     */
    Map<String, Object> getOverview();

    /**
     * 获取最近7天趋势数据
     * 返回：dates, supportCounts, amounts
     */
    Map<String, Object> getTrend();

    /**
     * 获取筹款分类统计
     * 返回：categoryId, categoryName, count, amount
     */
    Map<String, Object> getCategoryStats();

    /**
     * 获取待审核数量
     * 返回：pendingCampaigns, pendingWithdraws, pendingReports
     */
    Map<String, Object> getPendingCounts();
}