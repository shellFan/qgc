package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钱包流水类型
 */
@Getter
@AllArgsConstructor
public enum WalletFlowType {
    CAMPAIGN_INCOME("筹款收入"),
    WITHDRAW_APPLY("提现申请"),
    WITHDRAW_SUCCESS("提现成功"),
    WITHDRAW_FAIL_RETURN("提现失败退回"),
    PAYMENT_REFUND("支付退款"),
    WECHAT_FEE("微信手续费"),
    ADMIN_ADJUST("管理员调整");

    private final String desc;
}