package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.admin.service.AdminDashboardService;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.enums.WithdrawStatus;
import com.qiongguichou.content.mapper.ReportMapper;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import com.qiongguichou.wallet.entity.WithdrawOrder;
import com.qiongguichou.wallet.mapper.WithdrawOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台-数据统计服务实现
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserMapper userMapper;
    private final CampaignMapper campaignMapper;
    private final WithdrawOrderMapper withdrawOrderMapper;
    private final ReportMapper reportMapper;

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        // 总用户数
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        result.put("totalUsers", totalUsers);

        // 总筹款数
        Long totalCampaigns = campaignMapper.selectCount(new LambdaQueryWrapper<>());
        result.put("totalCampaigns", totalCampaigns);

        // 进行中筹款数
        Long activeCampaigns = campaignMapper.selectCount(
                new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
        );
        result.put("activeCampaigns", activeCampaigns);

        // 待审核筹款数
        Long pendingCampaigns = campaignMapper.selectCount(
                new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, CampaignStatus.PENDING_REVIEW.name())
        );
        result.put("pendingCampaigns", pendingCampaigns);

        // 待审核提现数
        Long pendingWithdraws = withdrawOrderMapper.selectCount(
                new LambdaQueryWrapper<WithdrawOrder>().eq(WithdrawOrder::getStatus, WithdrawStatus.PENDING.name())
        );
        result.put("pendingWithdraws", pendingWithdraws);

        // 今日新增用户
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)
        );
        result.put("todayUsers", todayUsers);

        // 已筹总金额（分）
        List<Campaign> campaigns = campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>().select(Campaign::getRaisedAmount)
        );
        long totalAmount = campaigns.stream()
                .mapToLong(c -> c.getRaisedAmount() != null ? c.getRaisedAmount() : 0L)
                .sum();
        result.put("totalAmount", totalAmount);

        return result;
    }

    @Override
    public Map<String, Object> getTrend() {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Long> supportCounts = new ArrayList<>();
        List<Long> amounts = new ArrayList<>();

        // 最近7天趋势（简化实现，实际应使用SQL聚合查询）
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            // 每日数据需要SQL聚合，此处简化为0
            supportCounts.add(0L);
            amounts.add(0L);
        }

        result.put("dates", dates);
        result.put("supportCounts", supportCounts);
        result.put("amounts", amounts);
        return result;
    }

    @Override
    public Map<String, Object> getCategoryStats() {
        Map<String, Object> result = new HashMap<>();
        // 分类统计需要SQL JOIN聚合查询，此处返回空结果
        // 实际生产应使用 campaignMapper 自定义SQL
        result.put("categories", new ArrayList<>());
        return result;
    }

    @Override
    public Map<String, Object> getPendingCounts() {
        Map<String, Object> result = new HashMap<>();

        Long pendingCampaigns = campaignMapper.selectCount(
                new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, CampaignStatus.PENDING_REVIEW.name())
        );
        result.put("pendingCampaigns", pendingCampaigns);

        Long pendingWithdraws = withdrawOrderMapper.selectCount(
                new LambdaQueryWrapper<WithdrawOrder>().eq(WithdrawOrder::getStatus, WithdrawStatus.PENDING.name())
        );
        result.put("pendingWithdraws", pendingWithdraws);

        Long pendingReports = reportMapper.selectCount(new LambdaQueryWrapper<>());
        result.put("pendingReports", pendingReports);

        return result;
    }
}