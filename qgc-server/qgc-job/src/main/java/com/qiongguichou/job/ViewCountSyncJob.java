package com.qiongguichou.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.core.config.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 浏览量同步定时任务
 * 每5分钟将Redis中的浏览量增量同步到MySQL
 */
@Slf4j
@Component
public class ViewCountSyncJob {

    @Autowired
    private RedisService redisService;

    @Autowired
    private CampaignMapper campaignMapper;

    /**
     * 每5分钟执行：Redis浏览量 → MySQL
     * Key格式: qgc:campaign:views:{campaignId}
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncViewCount() {
        log.info("开始执行浏览量同步任务");

        try {
            // 获取所有浏览量缓存Key
            Set<String> keys = redisService.getKeysByPattern("qgc:campaign:views:*");
            if (keys == null || keys.isEmpty()) {
                log.info("无浏览量缓存需要同步");
                return;
            }

            int syncCount = 0;
            for (String key : keys) {
                try {
                    // 从Key中提取campaignId: qgc:campaign:views:{id}
                    String campaignIdStr = key.substring("qgc:campaign:views:".length());
                    Long campaignId = Long.parseLong(campaignIdStr);

                    // 获取Redis中的浏览量增量
                    Integer viewCount = redisService.getViewCount(campaignId);
                    if (viewCount == null || viewCount <= 0) {
                        continue;
                    }

                    // 更新MySQL中的浏览量（累加）
                    LambdaUpdateWrapper<Campaign> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(Campaign::getId, campaignId)
                            .setSql("view_count = view_count + " + viewCount);
                    int updated = campaignMapper.update(null, wrapper);

                    if (updated > 0) {
                        // 同步成功，清除Redis缓存
                        redisService.clearViewCount(campaignId);
                        syncCount++;
                    }
                } catch (Exception e) {
                    log.error("同步浏览量失败, key={}", key, e);
                }
            }

            log.info("浏览量同步任务完成: syncCount={}", syncCount);
        } catch (Exception e) {
            log.error("浏览量同步任务执行失败", e);
        }
    }
}