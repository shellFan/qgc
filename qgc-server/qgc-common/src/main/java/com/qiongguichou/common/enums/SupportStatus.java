package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支持订单状态
 */
@Getter
@AllArgsConstructor
public enum SupportStatus {
    CREATED("已创建"),
    PAID("已支付"),
    CLOSED("已关闭"),
    REFUNDING("退款中"),
    REFUNDED("已退款");

    private final String desc;
}