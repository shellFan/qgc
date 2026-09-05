package com.qiongguichou.campaign.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 返图详情VO
 */
@Data
public class ProofDetailVO {

    private Long id;

    private Long campaignId;

    private Long userId;

    private String title;

    private String content;

    private Integer likeCount;

    private Integer commentCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 创建者昵称
     */
    private String creatorNickname;

    /**
     * 创建者头像
     */
    private String creatorAvatar;

    /**
     * 筹款项目标题
     */
    private String campaignTitle;

    /**
     * 筹款目标金额(分)
     */
    private Long campaignTargetAmount;

    /**
     * 筹款已筹金额(分)
     */
    private Long campaignRaisedAmount;

    /**
     * 筹款目标金额(元)
     */
    public BigDecimal getCampaignTargetAmountYuan() {
        return campaignTargetAmount != null ? new BigDecimal(campaignTargetAmount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 筹款已筹金额(元)
     */
    public BigDecimal getCampaignRaisedAmountYuan() {
        return campaignRaisedAmount != null ? new BigDecimal(campaignRaisedAmount).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
    }
}