package com.qiongguichou.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户钱包表
 */
@Data
@TableName("qgc_user_wallet")
public class Wallet {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 可提现余额(分) */
    private Long balance;

    /** 冻结金额(分) */
    private Long frozenAmount;

    /** 累计收入(分) */
    private Long totalIncome;

    /** 累计提现(分) */
    private Long totalWithdraw;

    /** 累计投喂别人(分) */
    private Long totalSupport;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}