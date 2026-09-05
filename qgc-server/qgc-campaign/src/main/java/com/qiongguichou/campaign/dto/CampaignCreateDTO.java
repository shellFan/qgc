package com.qiongguichou.campaign.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建筹款请求DTO
 */
@Data
public class CampaignCreateDTO {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题最长128个字符")
    private String title;

    /**
     * 介绍
     */
    @NotBlank(message = "介绍不能为空")
    @Size(max = 5000, message = "介绍最长5000个字符")
    private String description;

    /**
     * 封面图URL
     */
    @NotBlank(message = "封面图不能为空")
    private String cover;

    /**
     * 图片URL列表
     */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;

    /**
     * 目标金额(元)
     */
    @NotNull(message = "目标金额不能为空")
    @DecimalMin(value = "0.01", message = "目标金额最低0.01元")
    @DecimalMax(value = "100.00", message = "目标金额最高100元")
    private BigDecimal targetAmount;

    /**
     * 筹款时长(小时): 24/48/72
     */
    @NotNull(message = "筹款时长不能为空")
    private Integer durationHours;

    /**
     * 公开范围: PUBLIC/PRIVATE_LINK
     */
    private String visibility = "PUBLIC";

    /**
     * 允许排行榜 0否 1是
     */
    private Integer allowRanking = 1;

    /**
     * 允许评论 0否 1是
     */
    private Integer allowComment = 1;
}