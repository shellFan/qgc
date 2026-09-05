package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 筹款状态枚举
 */
@Getter
@AllArgsConstructor
public enum CampaignStatus {
    DRAFT("草稿"),
    PENDING_REVIEW("待审核"),
    ACTIVE("进行中"),
    SUCCESS("已筹满"),
    EXPIRED("已到期"),
    CLOSED("已关闭"),
    REJECTED("审核拒绝"),
    RISK_FROZEN("风控冻结");

    private final String desc;

    public static CampaignStatus fromValue(String value) {
        for (CampaignStatus status : values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CampaignStatus: " + value);
    }
}