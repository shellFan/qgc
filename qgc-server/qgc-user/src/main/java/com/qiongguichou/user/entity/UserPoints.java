package com.qiongguichou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分实体
 */
@Data
@TableName("qgc_user_points")
public class UserPoints {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer points;

    private Integer totalEarned;

    private Integer totalSpent;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}