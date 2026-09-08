package com.qiongguichou.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付回调日志表 - 记录微信支付/退款回调原始数据
 */
@Data
@TableName("qgc_pay_notify_log")
public class PayNotifyLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 类型: PAY/REFUND */
    private String type;

    /** 关联订单号 */
    private String orderNo;

    /** 微信交易号 */
    private String transactionId;

    /** 原始回调数据 */
    private String rawData;

    /** 处理结果: SUCCESS/FAIL */
    private String processResult;

    /** 错误信息 */
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}