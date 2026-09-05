package com.qiongguichou.campaign.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qiongguichou.common.enums.CampaignStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 筹款项目实体
 */
@Data
@TableName("qgc_campaign")
public class Campaign implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目编号
     */
    private String campaignNo;

    /**
     * 发起人用户ID
     */
    private Long creatorUserId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标题
     */
    private String title;

    /**
     * 介绍
     */
    private String description;

    /**
     * 封面图URL
     */
    private String cover;

    /**
     * 目标金额(分)
     */
    private Long targetAmount;

    /**
     * 已筹金额(分)
     */
    private Long raisedAmount;

    /**
     * 支持人数
     */
    private Integer supportCount;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 筹款时长(小时)
     */
    private Integer durationHours;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 公开范围: PUBLIC/PRIVATE_LINK
     */
    private String visibility;

    /**
     * 允许排行榜 0否 1是
     */
    private Integer allowRanking;

    /**
     * 允许评论 0否 1是
     */
    private Integer allowComment;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 返图状态 0未返图 1已返图
     */
    private Integer proofStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long createBy;

    private Long updateBy;

    @TableLogic
    private Integer deleted;

    /**
     * 获取状态枚举
     */
    public CampaignStatus getStatusEnum() {
        return status != null ? CampaignStatus.fromValue(status) : null;
    }

    /**
     * 设置状态枚举
     */
    public void setStatusEnum(CampaignStatus campaignStatus) {
        this.status = campaignStatus != null ? campaignStatus.name() : null;
    }
}