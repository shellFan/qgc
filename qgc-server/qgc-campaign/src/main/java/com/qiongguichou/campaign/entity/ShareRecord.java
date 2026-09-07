package com.qiongguichou.campaign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分享记录实体
 */
@Data
@TableName("qgc_share_record")
public class ShareRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long campaignId;

    private String shareType;

    private String source;

    private String shareCode;

    private Integer visitorCount;

    private Integer supportCount;

    private Long supportAmount;

    private LocalDateTime createTime;
}