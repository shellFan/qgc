package com.qiongguichou.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qgc_blacklist")
public class Blacklist {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String openid;

    private String reason;

    private String type;

    private String value;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}