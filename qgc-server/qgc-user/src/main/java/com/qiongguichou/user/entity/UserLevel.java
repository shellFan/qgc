package com.qiongguichou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户等级实体
 */
@Data
@TableName("qgc_user_level")
public class UserLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer level;

    private Integer exp;

    private String title;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}