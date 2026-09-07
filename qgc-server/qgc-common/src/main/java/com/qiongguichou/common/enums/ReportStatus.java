package com.qiongguichou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 举报状态
 */
@Getter
@AllArgsConstructor
public enum ReportStatus {
    PENDING("待处理"),
    REVIEWED("已审核"),
    RESOLVED("已处理");

    private final String desc;
}