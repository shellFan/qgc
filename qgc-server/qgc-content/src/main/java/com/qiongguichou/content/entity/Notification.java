package com.qiongguichou.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知表
 */
@Data
@TableName("qgc_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 类型: SUPPORT/SUCCESS/EXPIRE/COMMENT/LIKE/WITHDRAW/REVIEW/ADMIN */
    private String type;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 关联ID */
    private Long relatedId;

    /** 关联类型 */
    private String relatedType;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}