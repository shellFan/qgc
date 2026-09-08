package com.qiongguichou.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付订单表 - 微信支付统一下单记录
 */
@Data
@TableName("qgc_payment_order")
public class PaymentOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付订单号 */
    private String orderNo;

    /** 关联支持订单号 */
    private String supportNo;

    /** 筹款ID */
    private Long campaignId;

    /** 支持者用户ID */
    private Long supporterUserId;

    /** 发起人用户ID */
    private Long creatorUserId;

    /** 支付金额(分) */
    private Long amount;

    /** 有效金额(分) */
    private Long effectiveAmount;

    /** 退款金额(分) */
    private Long refundAmount;

    /** 交易类型 */
    private String tradeType;

    /** 支付类型: MOCK/NATIVE/JSAPI */
    private String payType;

    /** 支付者openid */
    private String openid;

    /** Native支付二维码URL(weixin://wxpay/...) */
    private String codeUrl;

    /** Native支付过期时间 */
    private LocalDateTime expireTime;

    /** 支付请求ID(客户端生成，幂等防重复) */
    private String requestId;

    /** 预支付ID(prepay_id) */
    private String prepayId;

    /** 微信支付交易号 */
    private String transactionId;

    /** 支付状态: CREATED/PAYING/SUCCESS/CLOSED/REFUNDING/PART_REFUNDED/REFUNDED/FAIL */
    private String status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 回调时间 */
    private LocalDateTime notifyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}