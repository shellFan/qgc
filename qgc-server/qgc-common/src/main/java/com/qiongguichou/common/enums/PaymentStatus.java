package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付订单状态
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {
    CREATED("已创建"),
    PAYING("支付中"),
    SUCCESS("支付成功"),
    CLOSED("已关闭"),
    REFUNDING("退款中"),
    PART_REFUNDED("部分退款"),
    REFUNDED("已退款"),
    FAIL("支付失败");

    private final String desc;
}