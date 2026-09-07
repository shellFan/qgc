package com.qiongguichou.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.core.entity.RankingSnapshot;
import com.qiongguichou.core.mapper.RankingSnapshotMapper;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 排行榜生成定时任务
 * 每天凌晨1点生成当日排行榜
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingGenerateJob {

    private final CampaignMapper campaignMapper;
    private final UserMapper userMapper;
    private final RankingSnapshotMapper rankingSnapshotMapper;

    /**
     * 今日最惨榜 - 今日发起筹款金额最多的人
     */
    // @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点
    // 暂时注释掉自动执行，手动触发测试
    public void generateSaddestDayRanking() {
        log.info("开始生成今日最惨榜...");
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 查询今日发起的筹款，按目标金额排序
        List<Campaign> campaigns = campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .ge(Campaign::getCreateTime, todayStart)
                        .le(Campaign::getCreateTime, todayEnd)
                        .eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
                        .orderByDesc(Campaign::getTargetAmount)
                        .last("LIMIT 20"));

        int rank = 1;
        for (Campaign c : campaigns) {
            User user = userMapper.selectById(c.getCreatorUserId());
            if (user == null) continue;

            RankingSnapshot snapshot = new RankingSnapshot();
            snapshot.setRankingType("SADDEST_DAY");
            snapshot.setRankingDate(today);
            snapshot.setUserId(user.getId());
            snapshot.setNickname(user.getNickname());
            snapshot.setAvatar(user.getAvatar());
            snapshot.setRankNo(rank++);
            snapshot.setScoreValue(c.getTargetAmount());
            snapshot.setScoreLabel(formatFen(c.getTargetAmount()) + "元");
            snapshot.setAnonymous(c.getAllowRanking() == 0 ? 1 : 0);

            rankingSnapshotMapper.insert(snapshot);
        }
        log.info("今日最惨榜生成完成, 共{}条", campaigns.size());
    }

    /**
     * 最多义父榜 - 累计支持人数最多
     */
    // @Scheduled(cron = "0 5 1 * * ?")
    public void generateMostSupportersRanking() {
        log.info("开始生成最多义父榜...");
        LocalDate today = LocalDate.now();

        // 使用自定义SQL查询支持人数最多的用户
        // 简化实现：查询活跃筹款中support_count最高的
        List<Campaign> campaigns = campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
                        .orderByDesc(Campaign::getSupportCount)
                        .last("LIMIT 20"));

        int rank = 1;
        for (Campaign c : campaigns) {
            User user = userMapper.selectById(c.getCreatorUserId());
            if (user == null) continue;

            RankingSnapshot snapshot = new RankingSnapshot();
            snapshot.setRankingType("MOST_SUPPORTERS");
            snapshot.setRankingDate(today);
            snapshot.setUserId(user.getId());
            snapshot.setNickname(user.getNickname());
            snapshot.setAvatar(user.getAvatar());
            snapshot.setRankNo(rank++);
            snapshot.setScoreValue((long) c.getSupportCount());
            snapshot.setScoreLabel(c.getSupportCount() + "位义父");
            snapshot.setAnonymous(c.getAllowRanking() == 0 ? 1 : 0);

            rankingSnapshotMapper.insert(snapshot);
        }
        log.info("最多义父榜生成完成, 共{}条", campaigns.size());
    }

    private String formatFen(Long fen) {
        if (fen == null) return "0";
        return String.format("%.2f", fen / 100.0);
    }
}