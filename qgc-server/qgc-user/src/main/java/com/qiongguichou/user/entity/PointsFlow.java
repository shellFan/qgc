package com.qiongguichou.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水实体
 */
@Data
@TableName("qgc_points_flow")
public class PointsFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private Integer amount;

    private Integer balanceAfter;

    private String relatedId;

    private String remark;

    private LocalDateTime createTime;
}