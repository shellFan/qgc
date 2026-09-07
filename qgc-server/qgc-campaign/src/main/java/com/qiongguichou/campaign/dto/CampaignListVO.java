package com.qiongguichou.campaign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 筹款列表项VO
 */
@Data
public class CampaignListVO {

    private Long id;

    private String campaignNo;

    private String title;

    private String cover;

    /**
     * 创建者用户ID
     */
    private Long creatorUserId;

    /**
     * 创建者昵称
     */
    private String creatorNickname;

    /**
     * 创建者头像
     */
    private String creatorAvatar;

    /**
     * 目标金额(分)
     */
    private Long targetAmount;

    /**
     * 已筹金额(分)
     */
    private Long raisedAmount;

    /**
     * 剩余金额(分) - 计算字段
     */
    private Long remainingAmount;

    /**
     * 进度百分比(0-100) - 计算字段
     */
    private Integer progressPercent;

    /**
     * 支持人数
     */
    private Integer supportCount;

    /**
     * 剩余时间(秒) - 计算字段
     */
    private Long remainingTime;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 状态
     */
    private String status;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 目标金额(元)
     */
    public BigDecimal getTargetAmountYuan() {
        return targetAmount != null ? new BigDecimal(targetAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 已筹金额(元)
     */
    public BigDecimal getRaisedAmountYuan() {
        return raisedAmount != null ? new BigDecimal(raisedAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 剩余金额(元)
     */
    public BigDecimal getRemainingAmountYuan() {
        return remainingAmount != null ? new BigDecimal(remainingAmount).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}