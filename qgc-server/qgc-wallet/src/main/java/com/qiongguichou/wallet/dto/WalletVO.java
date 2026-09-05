package com.qiongguichou.wallet.dto;

import lombok.Data;

/**
 * 钱包信息VO
 */
@Data
public class WalletVO {

    /** 钱包ID */
    private Long id;

    /** 可提现余额(分) */
    private Long balance;

    /** 可提现余额(元) */
    private String balanceYuan;

    /** 冻结金额(分) */
    private Long frozenAmount;

    /** 冻结金额(元) */
    private String frozenAmountYuan;

    /** 累计收入(分) */
    private Long totalIncome;

    /** 累计收入(元) */
    private String totalIncomeYuan;

    /** 累计提现(分) */
    private Long totalWithdraw;

    /** 累计提现(元) */
    private String totalWithdrawYuan;

    /** 累计投喂别人(分) */
    private Long totalSupport;

    /** 累计投喂别人(元) */
    private String totalSupportYuan;
}