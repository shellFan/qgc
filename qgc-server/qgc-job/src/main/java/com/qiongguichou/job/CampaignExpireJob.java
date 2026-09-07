package com.qiongguichou.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.common.enums.CampaignStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 筹款过期定时任务
 * 每分钟检查：已到期的ACTIVE筹款→EXPIRED，已筹满的→SUCCESS
 */
@Slf4j
@Component
public class CampaignExpireJob {

    @Autowired
    private CampaignMapper campaignMapper;

    /**
     * 每分钟执行：关闭过期筹款
     * 1. ACTIVE且endTime < now且未筹满 → EXPIRED
     * 2. ACTIVE且已筹满 → SUCCESS
     * 3. EXPIRED且已筹满 → SUCCESS（兜底）
     */
    @Scheduled(cron = "0 * * * * ?")
    public void closeExpiredCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        log.info("开始执行过期筹款关闭任务, time={}", now);

        try {
            // 1. 关闭已到期但未筹满的筹款: ACTIVE → EXPIRED
            // 使用apply进行列比较（raised_amount < target_amount），兼容所有MyBatis-Plus版本
            LambdaUpdateWrapper<Campaign> expireWrapper = new LambdaUpdateWrapper<>();
            expireWrapper.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
                    .lt(Campaign::getEndTime, now)
                    .apply("raised_amount < target_amount")
                    .set(Campaign::getStatus, CampaignStatus.EXPIRED.name())
                    .set(Campaign::getUpdateTime, now);
            int expiredCount = campaignMapper.update(null, expireWrapper);

            // 2. 标记已筹满的筹款: ACTIVE → SUCCESS
            LambdaUpdateWrapper<Campaign> successWrapper = new LambdaUpdateWrapper<>();
            successWrapper.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
                    .apply("raised_amount >= target_amount")
                    .set(Campaign::getStatus, CampaignStatus.SUCCESS.name())
                    .set(Campaign::getUpdateTime, now);
            int successCount = campaignMapper.update(null, successWrapper);

            // 3. 已到期且已筹满的筹款: EXPIRED → SUCCESS (可能上一轮漏掉的)
            LambdaUpdateWrapper<Campaign> lateSuccessWrapper = new LambdaUpdateWrapper<>();
            lateSuccessWrapper.eq(Campaign::getStatus, CampaignStatus.EXPIRED.name())
                    .apply("raised_amount >= target_amount")
                    .set(Campaign::getStatus, CampaignStatus.SUCCESS.name())
                    .set(Campaign::getUpdateTime, now);
            int lateSuccessCount = campaignMapper.update(null, lateSuccessWrapper);

            log.info("过期筹款关闭任务完成: expired={}, success={}, lateSuccess={}",
                    expiredCount, successCount, lateSuccessCount);
        } catch (Exception e) {
            log.error("过期筹款关闭任务执行失败", e);
        }
    }
}