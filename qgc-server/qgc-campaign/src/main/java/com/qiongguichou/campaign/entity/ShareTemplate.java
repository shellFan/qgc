package com.qiongguichou.campaign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qgc_share_template")
public class ShareTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;

    private String titleTemplate;

    private String descTemplate;

    private String imageUrl;

    private Integer sort;

    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}