package com.qiongguichou.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提现订单表
 */
@Data
@TableName("qgc_withdraw_order")
public class WithdrawOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提现单号 */
    private String withdrawNo;

    /** 提现用户ID */
    private Long userId;

    /** 提现金额(分) */
    private Long amount;

    /** 手续费(分) */
    private Long fee;

    /** 实际到账(分) */
    private Long actualAmount;

    /** 提现状态: PENDING/PROCESSING/SUCCESS/FAIL/REJECTED */
    private String status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 转账流水号 */
    private String transferNo;

    /** 转账时间 */
    private LocalDateTime transferTime;

    /** 审核人ID */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}