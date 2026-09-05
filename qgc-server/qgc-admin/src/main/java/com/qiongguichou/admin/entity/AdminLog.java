package com.qiongguichou.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志表
 */
@Data
@TableName("qgc_admin_log")
public class AdminLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 管理员ID */
    private Long adminId;

    /** 管理员用户名 */
    private String adminUsername;

    /** 操作 */
    private String action;

    /** 目标类型 */
    private String targetType;

    /** 目标ID */
    private String targetId;

    /** 操作前数据 */
    private String beforeData;

    /** 操作后数据 */
    private String afterData;

    /** IP */
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}