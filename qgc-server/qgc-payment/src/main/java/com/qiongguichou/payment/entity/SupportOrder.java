package com.qiongguichou.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支持订单表 - 记录用户对筹款的投喂支持
 */
@Data
@TableName("qgc_support_order")
public class SupportOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支持订单号 */
    private String supportNo;

    /** 筹款ID */
    private Long campaignId;

    /** 支持者用户ID */
    private Long supporterUserId;

    /** 发起人用户ID */
    private Long creatorUserId;

    /** 支持金额(分) */
    private Long amount;

    /** 有效金额(分) */
    private Long effectiveAmount;

    /** 退款金额(分) */
    private Long refundAmount;

    /** 是否匿名 0否1是 */
    private Integer anonymous;

    /** 是否隐藏金额 0否1是 */
    private Integer hideAmount;

    /** 留言 */
    private String message;

    /** 订单状态: CREATED/PAID/REFUNDED/PART_REFUNDED/CLOSED */
    private String status;

    /** 支付时间 */
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}