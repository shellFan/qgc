package com.qiongguichou.campaign.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.campaign.entity.ShareRecord;
import com.qiongguichou.campaign.mapper.ShareRecordMapper;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.core.config.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分享裂变服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService {

    private final ShareRecordMapper shareRecordMapper;
    private final RedisService redisService;

    /** 访客去重Redis key前缀, TTL 24h */
    private static final String VISIT_DEDUP_KEY = "qgc:share:visit:";
    private static final long VISIT_DEDUP_TTL_HOURS = 24;
    /** visitorKey最大原始长度, 超过则SHA-256压缩 */
    private static final int VISITOR_KEY_MAX_RAW_LEN = 64;

    /**
     * 记录分享行为
     */
    public ShareRecord recordShare(Long userId, Long campaignId, String shareType, String source) {
        ShareRecord record = new ShareRecord();
        record.setUserId(userId);
        record.setCampaignId(campaignId);
        record.setShareType(shareType);
        record.setSource(source);
        record.setShareCode(generateShareCode());
        record.setVisitorCount(0);
        record.setSupportCount(0);
        record.setSupportAmount(0L);
        shareRecordMapper.insert(record);

        // 积分奖励: 分享获得经验
        // 由调用方处理积分逻辑
        return record;
    }

    /**
     * 通过分享码访问(记录访客，Redis SET NX 24h去重)
     * @param shareCode 分享码
     * @param visitorKey 访客标识(userId或匿名visitorId)
     */
    public void trackVisit(String shareCode, String visitorKey) {
        if (visitorKey == null || visitorKey.isEmpty()) {
            // 无访客标识则无法去重，使用IP作为兜底
            visitorKey = "ip:" + (UserContext.getIp() != null ? UserContext.getIp() : UUID.randomUUID().toString());
        }

        // 安全处理visitorKey: 超长则SHA-256压缩, 防止恶意构造超长key撑爆Redis
        String safeVisitorKey = safeVisitorKey(visitorKey);

        // Redis SET NX原子去重: 同一访客对同一分享码24h内只计1次
        String dedupKey = VISIT_DEDUP_KEY + shareCode + ":" + safeVisitorKey;
        Boolean acquired = redisService.setIfAbsent(dedupKey, "1", VISIT_DEDUP_TTL_HOURS, TimeUnit.HOURS);
        if (acquired == null || !acquired) {
            log.debug("访客去重跳过: shareCode={}, visitorKey={}", shareCode, safeVisitorKey);
            return;
        }

        ShareRecord record = shareRecordMapper.selectOne(
                new LambdaQueryWrapper<ShareRecord>().eq(ShareRecord::getShareCode, shareCode));
        if (record != null) {
            record.setVisitorCount(record.getVisitorCount() + 1);
            shareRecordMapper.updateById(record);
        } else {
            // 分享码不存在, 清除去重标记让下次可重试
            redisService.delete(dedupKey);
        }
    }

    /**
     * 通过分享码投喂(记录支持，同一supportOrder只计1次, SET NX原子去重)
     */
    public void trackSupport(String shareCode, long amountFen, String supportNo) {
        // Redis SET NX原子去重: 同一支持订单对同一分享码只计1次
        String dedupKey = "qgc:share:support:" + shareCode + ":" + supportNo;
        Boolean acquired = redisService.setIfAbsent(dedupKey, "1", 7, TimeUnit.DAYS);
        if (acquired == null || !acquired) {
            return;
        }

        ShareRecord record = shareRecordMapper.selectOne(
                new LambdaQueryWrapper<ShareRecord>().eq(ShareRecord::getShareCode, shareCode));
        if (record != null) {
            record.setSupportCount(record.getSupportCount() + 1);
            record.setSupportAmount(record.getSupportAmount() + amountFen);
            shareRecordMapper.updateById(record);
        } else {
            // 分享码不存在, 清除去重标记让下次可重试
            redisService.delete(dedupKey);
        }
    }

    /**
     * 获取用户的分享统计
     */
    public ShareStats getUserShareStats(Long userId) {
        List<ShareRecord> records = shareRecordMapper.selectList(
                new LambdaQueryWrapper<ShareRecord>().eq(ShareRecord::getUserId, userId));

        ShareStats stats = new ShareStats();
        stats.setTotalShares(records.size());
        stats.setTotalVisitors(records.stream().mapToInt(ShareRecord::getVisitorCount).sum());
        stats.setTotalSupporters(records.stream().mapToInt(ShareRecord::getSupportCount).sum());
        stats.setTotalSupportAmount(records.stream().mapToLong(ShareRecord::getSupportAmount).sum());
        return stats;
    }

    /**
     * 获取筹款的分享记录
     */
    public List<ShareRecord> getCampaignShareRecords(Long campaignId) {
        return shareRecordMapper.selectList(
                new LambdaQueryWrapper<ShareRecord>()
                        .eq(ShareRecord::getCampaignId, campaignId)
                        .orderByDesc(ShareRecord::getCreateTime));
    }

    /**
     * 安全处理visitorKey: 超长则SHA-256压缩, 防止恶意构造超长key撑爆Redis
     * SHA-256输出32字节, Base64编码后44字符, 始终在安全范围内
     */
    private String safeVisitorKey(String visitorKey) {
        if (visitorKey.length() <= VISITOR_KEY_MAX_RAW_LEN) {
            return visitorKey;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(visitorKey.getBytes(StandardCharsets.UTF_8));
            return "h:" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256必须存在, 降级为截断
            log.warn("SHA-256不可用, 降级截断visitorKey", e);
            return visitorKey.substring(0, VISITOR_KEY_MAX_RAW_LEN);
        }
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * 分享统计VO
     */
    public static class ShareStats {
        private int totalShares;
        private int totalVisitors;
        private int totalSupporters;
        private long totalSupportAmount;

        public int getTotalShares() { return totalShares; }
        public void setTotalShares(int totalShares) { this.totalShares = totalShares; }
        public int getTotalVisitors() { return totalVisitors; }
        public void setTotalVisitors(int totalVisitors) { this.totalVisitors = totalVisitors; }
        public int getTotalSupporters() { return totalSupporters; }
        public void setTotalSupporters(int totalSupporters) { this.totalSupporters = totalSupporters; }
        public long getTotalSupportAmount() { return totalSupportAmount; }
        public void setTotalSupportAmount(long totalSupportAmount) { this.totalSupportAmount = totalSupportAmount; }
    }
}