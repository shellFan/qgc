package com.qiongguichou.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiongguichou.content.entity.AdStat;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdStatMapper extends BaseMapper<AdStat> {

    /**
     * 原子upsert广告统计(INSERT ON DUPLICATE KEY UPDATE)
     */
    @Insert("INSERT INTO qgc_ad_stat (ad_id, stat_date, impression_count, click_count, create_time, update_time) " +
            "VALUES (#{adId}, #{statDate}, #{impressionCount}, #{clickCount}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "impression_count = impression_count + VALUES(impression_count), " +
            "click_count = click_count + VALUES(click_count), " +
            "update_time = NOW()")
    int upsertByAdAndDate(AdStat adStat);
}