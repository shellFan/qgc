package com.qiongguichou.campaign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 筹款分类实体
 */
@Data
@TableName("qgc_campaign_category")
public class CampaignCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 封面图
     */
    private String cover;

    /**
     * 主题色
     */
    private String themeColor;

    /**
     * 默认标题模板
     */
    private String defaultTitle;

    /**
     * 默认描述模板
     */
    private String defaultDescription;

    /**
     * 分享标题模板
     */
    private String shareTitleTemplate;

    /**
     * 分享描述模板
     */
    private String shareDescTemplate;

    /**
     * 排序(越小越前)
     */
    private Integer sort;

    /**
     * 是否启用 0禁用 1启用
     */
    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}