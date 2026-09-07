package com.qiongguichou.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.content.entity.Ad;
import com.qiongguichou.content.entity.AdPosition;
import com.qiongguichou.content.entity.AdStat;
import com.qiongguichou.content.mapper.AdMapper;
import com.qiongguichou.content.mapper.AdPositionMapper;
import com.qiongguichou.content.mapper.AdStatMapper;
import com.qiongguichou.core.config.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 广告服务 - Redis统计优化(按日分区key+原子同步)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdMapper adMapper;
    private final AdPositionMapper adPositionMapper;
    private final AdStatMapper adStatMapper;
    private final RedisService redisService;

    private static final String AD_VIEW_KEY = "qgc:ad:view:";
    private static final String AD_CLICK_KEY = "qgc:ad:click:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 获取指定位置的广告列表
     */
    public List<Ad> getAdsByPosition(String positionCode) {
        AdPosition position = adPositionMapper.selectOne(
                new LambdaQueryWrapper<AdPosition>()
                        .eq(AdPosition::getCode, positionCode)
                        .eq(AdPosition::getEnabled, 1));
        if (position == null) {
            return new ArrayList<>();
        }

        return adMapper.selectList(
                new LambdaQueryWrapper<Ad>()
                        .eq(Ad::getPositionId, position.getId())
                        .eq(Ad::getEnabled, 1)
                        .le(Ad::getStartTime, java.time.LocalDateTime.now())
                        .ge(Ad::getEndTime, java.time.LocalDateTime.now())
                        .orderByAsc(Ad::getSort));
    }

    /**
     * 记录广告曝光(Redis, 按日分区key)
     */
    public void recordImpression(Long adId) {
        String key = AD_VIEW_KEY + adId + ":" + LocalDate.now().format(DATE_FMT);
        redisService.increment(key);
        // 当日key次日过期(保留2天防跨日边界问题)
        redisService.expire(key, 2, TimeUnit.DAYS);
    }

    /**
     * 记录广告点击(Redis, 按日分区key)
     */
    public void recordClick(Long adId) {
        String key = AD_CLICK_KEY + adId + ":" + LocalDate.now().format(DATE_FMT);
        redisService.increment(key);
        redisService.expire(key, 2, TimeUnit.DAYS);
    }

    /**
     * 获取广告当日Redis中的曝光数
     */
    public int getViewCount(Long adId) {
        String key = AD_VIEW_KEY + adId + ":" + LocalDate.now().format(DATE_FMT);
        String value = redisService.get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 获取广告当日Redis中的点击数
     */
    public int getClickCount(Long adId) {
        String key = AD_CLICK_KEY + adId + ":" + LocalDate.now().format(DATE_FMT);
        String value = redisService.get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 同步Redis广告统计到数据库(定时任务调用)
     * 原子操作: 使用GETSET读取并清零Redis计数，再upsert到DB
     */
    public void syncAdStats() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);

        // 扫描当日曝光key
        Set<String> viewKeys = redisService.getKeysByPattern(AD_VIEW_KEY + "*:" + dateStr);
        for (String viewKey : viewKeys) {
            try {
                // 解析adId: qgc:ad:view:{adId}:{dateStr}
                String keyBody = viewKey.substring(AD_VIEW_KEY.length());
                int lastColon = keyBody.lastIndexOf(':');
                if (lastColon <= 0) continue;
                Long adId = Long.parseLong(keyBody.substring(0, lastColon));

                // 读取并清零曝光计数(原子操作: GET + DEL)
                int viewCount = getViewCount(adId);
                if (viewCount > 0) {
                    redisService.delete(viewKey);
                }

                // 读取点击计数(同日)
                int clickCount = getClickCount(adId);
                String clickKey = AD_CLICK_KEY + adId + ":" + dateStr;
                if (clickCount > 0) {
                    redisService.delete(clickKey);
                }

                // 原子upsert到DB
                if (viewCount > 0 || clickCount > 0) {
                    AdStat stat = new AdStat();
                    stat.setAdId(adId);
                    stat.setStatDate(today);
                    stat.setImpressionCount(viewCount);
                    stat.setClickCount(clickCount);
                    adStatMapper.upsertByAdAndDate(stat);
                }
            } catch (Exception e) {
                log.error("同步广告统计失败, key={}", viewKey, e);
            }
        }
        log.info("广告统计同步完成, 共处理{}个key", viewKeys.size());
    }

    /**
     * 获取所有广告位
     */
    public List<AdPosition> getAllPositions() {
        return adPositionMapper.selectList(
                new LambdaQueryWrapper<AdPosition>().orderByAsc(AdPosition::getId));
    }

    /**
     * 获取所有广告
     */
    public List<Ad> getAllAds() {
        return adMapper.selectList(
                new LambdaQueryWrapper<Ad>().orderByDesc(Ad::getCreateTime));
    }

    /**
     * 创建广告
     */
    public Ad createAd(Ad ad) {
        adMapper.insert(ad);
        return ad;
    }

    /**
     * 更新广告
     */
    public void updateAd(Ad ad) {
        adMapper.updateById(ad);
    }

    /**
     * 删除广告
     */
    public void deleteAd(Long id) {
        adMapper.deleteById(id);
    }

    /**
     * 根据ID获取广告
     */
    public Ad getById(Long id) {
        return adMapper.selectById(id);
    }
}