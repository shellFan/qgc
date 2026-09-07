package com.qiongguichou.job;

import com.qiongguichou.content.service.AdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 广告统计同步定时任务
 * 每5分钟从Redis同步到MySQL
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdStatSyncJob {

    private final AdService adService;

    @Scheduled(fixedRate = 300000) // 5分钟
    public void syncAdStats() {
        try {
            log.info("开始同步广告统计...");
            adService.syncAdStats();
            log.info("广告统计同步完成");
        } catch (Exception e) {
            log.error("广告统计同步失败", e);
        }
    }
}