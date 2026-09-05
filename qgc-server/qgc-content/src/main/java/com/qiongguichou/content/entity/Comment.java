package com.qiongguichou.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论表
 */
@Data
@TableName("qgc_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标ID(筹款ID或返图ID) */
    private Long targetId;

    /** 目标类型: CAMPAIGN/PROOF */
    private String targetType;

    /** 评论用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 状态 1正常 2隐藏 3删除 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}