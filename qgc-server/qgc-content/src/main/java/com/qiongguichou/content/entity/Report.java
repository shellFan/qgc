package com.qiongguichou.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报表
 */
@Data
@TableName("qgc_report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报人ID */
    private Long reporterUserId;

    /** 目标ID */
    private Long targetId;

    /** 目标类型: CAMPAIGN/COMMENT/USER */
    private String targetType;

    /** 原因: FAKE/PORN/ILLEGAL/FRAUD/ADS/ATTACK/OTHER */
    private String reasonType;

    /** 补充说明 */
    private String reasonDesc;

    /** 状态: PENDING/CONFIRMED/IGNORED */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 处理备注 */
    private String handleRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}