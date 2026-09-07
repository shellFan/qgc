package com.qiongguichou.campaign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 筹款详情返回VO
 */
@Data
public class CampaignDetailVO {

    private Long id;

    private String campaignNo;

    private Long creatorUserId;

    private Long categoryId;

    private String title;

    private String description;

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

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String visibility;

    private Integer allowRanking;

    private Integer allowComment;

    private String status;

    private String rejectReason;

    private String closeReason;

    private Integer proofStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 创建者昵称
     */
    private String creatorNickname;

    /**
     * 创建者头像
     */
    private String creatorAvatar;

    /**
     * 剩余金额(分)
     */
    private Long remainingAmount;

    /**
     * 进度百分比(0-100)
     */
    private Integer progressPercent;

    /**
     * 是否为创建者
     */
    private Boolean isCreator;

    /**
     * 是否为支持者
     */
    private Boolean isSupporter;

    /**
     * 支持者排名(无则为null)
     */
    private Integer supporterRank;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 返图信息
     */
    private ProofDetailVO proofInfo;

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