package com.qiongguichou.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款订单表
 */
@Data
@TableName("qgc_refund_order")
public class RefundOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 退款单号 */
    private String refundNo;

    /** 关联支付订单号 */
    private String paymentOrderNo;

    /** 筹款ID */
    private Long campaignId;

    /** 退款用户ID */
    private Long userId;

    /** 退款金额(分) */
    private Long amount;

    /** 退款原因 */
    private String reason;

    /** 退款类型: OVERPAY/ADMIN/CLOSED */
    private String type;

    /** 微信退款单号 */
    private String wechatRefundId;

    /** 退款状态: PENDING/PROCESSING/SUCCESS/FAIL */
    private String status;

    /** 退款完成时间 */
    private LocalDateTime refundTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}