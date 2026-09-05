package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提现状态
 */
@Getter
@AllArgsConstructor
public enum WithdrawStatus {
    PENDING("待审核"),
    PROCESSING("处理中"),
    SUCCESS("成功"),
    FAIL("失败"),
    REJECTED("已拒绝");

    private final String desc;
}