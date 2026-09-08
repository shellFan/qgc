package com.qiongguichou.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误 10xxx
    SUCCESS(0, "success"),
    PARAM_ERROR(10001, "参数错误"),
    PARAM_MISSING(10002, "缺少必要参数"),
    PARAM_INVALID(10003, "参数格式不正确"),
    SYSTEM_ERROR(10004, "系统异常"),
    SYSTEM_BUSY(10005, "系统繁忙，请稍后重试"),
    TOO_MANY_REQUESTS(10006, "请求过于频繁"),
    NOT_FOUND(10007, "资源不存在"),

    // 认证错误 20xxx
    UNAUTHORIZED(20001, "未登录"),
    TOKEN_EXPIRED(20002, "登录已过期"),
    TOKEN_INVALID(20003, "无效的Token"),
    FORBIDDEN(20004, "无权限"),
    ADMIN_UNAUTHORIZED(20005, "管理员未登录"),
    ADMIN_FORBIDDEN(20006, "管理员无权限"),

    // 用户错误 30xxx
    USER_NOT_FOUND(30001, "用户不存在"),
    USER_BANNED(30002, "用户已被封禁"),
    USER_BLACKLISTED(30003, "用户已被拉黑"),

    // 筹款错误 40xxx
    CAMPAIGN_NOT_FOUND(40001, "筹款项目不存在"),
    CAMPAIGN_ENDED(40002, "筹款已结束"),
    CAMPAIGN_FULL(40003, "筹款已筹满"),
    CAMPAIGN_NOT_ACTIVE(40004, "筹款项目未激活"),
    CAMPAIGN_SELF_SUPPORT(40005, "自己V自己就没意思了"),
    CAMPAIGN_LIMIT_EXCEEDED(40006, "今日发起次数已达上限"),
    CAMPAIGN_CLOSED(40007, "筹款已关闭"),
    CAMPAIGN_REJECTED(40008, "筹款审核未通过"),
    CAMPAIGN_REVIEWING(40009, "筹款正在审核中"),

    // 支付错误 50xxx
    PAYMENT_CREATE_FAIL(50001, "创建支付订单失败"),
    PAYMENT_AMOUNT_INVALID(50002, "支付金额无效"),
    PAYMENT_AMOUNT_EXCEED(50003, "超出剩余可筹金额"),
    PAYMENT_ORDER_NOT_FOUND(50004, "支付订单不存在"),
    PAYMENT_DUPLICATE(50005, "重复支付"),
    PAYMENT_NOTIFY_INVALID(50006, "支付回调无效"),
    PAYMENT_NOTIFY_VERIFY_FAIL(50007, "支付回调验签失败"),
    PAYMENT_REFUND_FAIL(50008, "退款失败"),
    PAYMENT_PARAMS_ERROR(50009, "支付参数错误"),

    // 钱包错误 60xxx
    WALLET_NOT_FOUND(60001, "钱包不存在"),
    WALLET_INSUFFICIENT(60002, "余额不足"),
    WITHDRAW_AMOUNT_TOO_SMALL(60003, "提现金额低于最低限额"),
    WITHDRAW_DUPLICATE(60004, "请勿重复提交提现"),
    WITHDRAW_PROCESSING(60005, "提现正在处理中"),

    // 内容错误 70xxx
    COMMENT_NOT_FOUND(70001, "评论不存在"),
    COMMENT_NO_PERMISSION(70002, "无权删除此评论"),
    PROOF_NOT_FOUND(70003, "返图不存在"),
    PROOF_ALREADY_EXISTS(70004, "已提交返图"),
    SENSITIVE_WORD_DETECTED(70005, "内容包含敏感词"),
    REPORT_NOT_FOUND(70006, "举报不存在"),

    // 微信错误 80xxx
    WECHAT_AUTH_FAIL(80001, "微信授权失败"),
    WECHAT_PAY_FAIL(80002, "微信支付失败"),
    WECHAT_REFUND_FAIL(80003, "微信退款失败"),
    WECHAT_JSAPI_FAIL(80004, "微信JSAPI调用失败"),

    // 文件错误 90xxx
    FILE_TYPE_INVALID(90001, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(90002, "文件大小超限"),
    FILE_UPLOAD_FAIL(90003, "文件上传失败");

    private final int code;
    private final String message;
}