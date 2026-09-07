package com.qiongguichou.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排行榜快照实体
 */
@Data
@TableName("qgc_ranking_snapshot")
public class RankingSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String rankingType;

    private LocalDate rankingDate;

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer rankNo;

    private Long scoreValue;

    private String scoreLabel;

    private Integer anonymous;

    private LocalDateTime createTime;
}