package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款订单状态
 */
@Getter
@AllArgsConstructor
public enum RefundStatus {
    PENDING("待处理"),
    PROCESSING("退款中"),
    SUCCESS("退款成功"),
    FAIL("退款失败");

    private final String desc;
}