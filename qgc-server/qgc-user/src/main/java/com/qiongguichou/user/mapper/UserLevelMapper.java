package com.qiongguichou.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiongguichou.user.entity.UserLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserLevelMapper extends BaseMapper<UserLevel> {

    /**
     * 原子增加经验值并自动升级
     * 经验阈值: Lv1=0, Lv2=100, Lv3=500, Lv4=2000
     */
    @Update("UPDATE qgc_user_level SET exp = exp + #{exp}, " +
            "level = CASE WHEN exp + #{exp} >= 2000 THEN 4 " +
            "WHEN exp + #{exp} >= 500 THEN 3 " +
            "WHEN exp + #{exp} >= 100 THEN 2 " +
            "ELSE level END, " +
            "title = CASE WHEN exp + #{exp} >= 2000 THEN '终极义父' " +
            "WHEN exp + #{exp} >= 500 THEN '快乐投喂官' " +
            "WHEN exp + #{exp} >= 100 THEN '薯条伙伴' " +
            "ELSE title END, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int addExpAtomic(@Param("userId") Long userId, @Param("exp") int exp);
}