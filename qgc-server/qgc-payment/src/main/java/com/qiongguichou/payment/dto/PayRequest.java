package com.qiongguichou.payment.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 发起支付请求
 */
@Data
public class PayRequest {

    /** 筹款ID */
    @NotNull(message = "筹款ID不能为空")
    private Long campaignId;

    /** 支付金额(分) 1-10000 即0.01-100元 */
    @NotNull(message = "支付金额不能为空")
    @Min(value = 1, message = "最小支付1分")
    @Max(value = 10000, message = "最大支付100元")
    private Long amount;

    /** 留言 */
    private String message;

    /** 是否匿名 0否1是 */
    private Integer anonymous;
}