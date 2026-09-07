package com.qiongguichou.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("qgc_ad_stat")
public class AdStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long adId;

    private LocalDate statDate;

    private Long impressionCount;

    private Long clickCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}