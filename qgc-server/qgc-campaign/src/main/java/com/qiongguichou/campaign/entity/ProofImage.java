package com.qiongguichou.campaign.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 返图图片实体
 */
@Data
@TableName("qgc_proof_image")
public class ProofImage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 返图ID
     */
    private Long proofId;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 排序
     */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}