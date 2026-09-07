package com.qiongguichou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户徽章实体
 */
@Data
@TableName("qgc_user_badge")
public class UserBadge {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String badgeCode;

    private String badgeName;

    private String badgeIcon;

    private LocalDateTime earnedTime;

    private LocalDateTime createTime;
}