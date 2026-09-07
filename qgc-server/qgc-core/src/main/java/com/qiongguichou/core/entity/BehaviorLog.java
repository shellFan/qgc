package com.qiongguichou.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为日志实体
 */
@Data
@TableName("qgc_behavior_log")
public class BehaviorLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String event;

    private String targetId;

    private String targetType;

    private String source;

    private String ip;

    private String userAgent;

    private String deviceId;

    private String extra;

    private LocalDateTime createTime;
}