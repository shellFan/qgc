package com.qiongguichou.wallet.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 提现请求
 */
@Data
public class WithdrawRequest {

    /** 提现金额(分) 最小100(1元) 最大500000(5000元) */
    @NotNull(message = "提现金额不能为空")
    @Min(value = 100, message = "最小提现1元")
    @Max(value = 500000, message = "单次最大提现5000元")
    private Long amount;
}