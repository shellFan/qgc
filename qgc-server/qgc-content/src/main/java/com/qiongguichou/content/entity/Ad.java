package com.qiongguichou.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qgc_ad")
public class Ad {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long positionId;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private Integer sort;

    private Integer enabled;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}