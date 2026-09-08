package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付类型
 * MOCK: 开发环境模拟支付
 * NATIVE: 微信Native扫码支付(无需公众号/openid)
 * JSAPI: 微信JSAPI公众号支付(需要openid)
 */
@Getter
@AllArgsConstructor
public enum PayType {
    MOCK("模拟支付"),
    NATIVE("Native扫码支付"),
    JSAPI("JSAPI公众号支付");

    private final String desc;
}