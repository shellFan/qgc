package com.qiongguichou.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 钱包流水表 - 不可篡改，只插入不更新不删除
 */
@Data
@TableName("qgc_wallet_flow")
public class WalletFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 流水号 */
    private String flowNo;

    /** 用户ID */
    private Long userId;

    /** 流水类型: CAMPAIGN_INCOME/WITHDRAW_APPLY/WITHDRAW_SUCCESS/WITHDRAW_FAIL_RETURN/PAYMENT_REFUND/WECHAT_FEE/ADMIN_ADJUST */
    private String type;

    /** 变动金额(分),正为入,负为出 */
    private Long amount;

    /** 变动后余额(分) */
    private Long balanceAfter;

    /** 关联业务ID */
    private String relatedId;

    /** 关联业务类型 */
    private String relatedType;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}