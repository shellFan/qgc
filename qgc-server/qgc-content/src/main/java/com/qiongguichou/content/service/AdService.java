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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 广告服务 - Redis统计优化
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
     * 记录广告曝光(Redis)
     */
    public void recordImpression(Long adId) {
        String key = AD_VIEW_KEY + adId;
        redisService.increment(key);
        // 设置7天过期
        redisService.expire(key, 7, java.util.concurrent.TimeUnit.DAYS);
    }

    /**
     * 记录广告点击(Redis)
     */
    public void recordClick(Long adId) {
        String key = AD_CLICK_KEY + adId;
        redisService.increment(key);
        redisService.expire(key, 7, java.util.concurrent.TimeUnit.DAYS);
    }

    /**
     * 获取广告Redis中的统计数
     */
    public int getViewCount(Long adId) {
        String key = AD_VIEW_KEY + adId;
        String value = redisService.get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 获取广告Redis中的点击数
     */
    public int getClickCount(Long adId) {
        String key = AD_CLICK_KEY + adId;
        String value = redisService.get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 同步Redis广告统计到数据库(定时任务调用)
     */
    public void syncAdStats() {
        LocalDate today = LocalDate.now();

        // 扫描所有曝光key
        Set<String> viewKeys = redisService.getKeysByPattern(AD_VIEW_KEY + "*");
        for (String viewKey : viewKeys) {
            try {
                Long adId = Long.parseLong(viewKey.substring(AD_VIEW_KEY.length()));
                int viewCount = getViewCount(adId);
                int clickCount = getClickCount(adId);

                // 更新或插入当日统计
                AdStat existing = adStatMapper.selectOne(
                        new LambdaQueryWrapper<AdStat>()
                                .eq(AdStat::getAdId, adId)
                                .eq(AdStat::getStatDate, today));
                if (existing != null) {
                    existing.setImpressionCount(viewCount);
                    existing.setClickCount(clickCount);
                    adStatMapper.updateById(existing);
                } else {
                    AdStat stat = new AdStat();
                    stat.setAdId(adId);
                    stat.setStatDate(today);
                    stat.setImpressionCount(viewCount);
                    stat.setClickCount(clickCount);
                    adStatMapper.insert(stat);
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
}