package com.qiongguichou.campaign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 返图点赞实体
 */
@Data
@TableName("qgc_proof_like")
public class ProofLike implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 返图ID
     */
    private Long proofId;

    /**
     * 用户ID
     */
    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}