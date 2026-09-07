package com.qiongguichou.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qgc_risk_record")
public class RiskRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String riskType;

    private String riskLevel;

    private String description;

    private String ip;

    private String extraData;

    private String status;

    private Long handlerId;

    private LocalDateTime handleTime;

    private String handleRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}