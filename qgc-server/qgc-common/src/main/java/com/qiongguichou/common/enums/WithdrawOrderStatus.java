package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提现订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum WithdrawOrderStatus {
    PENDING("待审核"),
    PROCESSING("处理中"),
    SUCCESS("提现成功"),
    FAIL("提现失败"),
    REJECTED("审核拒绝");

    private final String desc;

    public static WithdrawOrderStatus fromValue(String value) {
        for (WithdrawOrderStatus status : values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown WithdrawOrderStatus: " + value);
    }
}