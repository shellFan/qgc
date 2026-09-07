package com.qiongguichou.content.controller;

import com.qiongguichou.common.enums.ReportStatus;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.content.entity.Report;
import com.qiongguichou.content.mapper.ReportMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 举报控制器
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportMapper reportMapper;

    /**
     * 提交举报
     */
    @PostMapping
    public Result<Void> submitReport(@RequestBody ReportRequest request) {
        Long userId = UserContext.getUserId();
        Report report = new Report();
        report.setTargetType(request.getTargetType());
        report.setTargetId(request.getTargetId());
        report.setReporterUserId(userId);
        report.setReasonType(request.getReason());
        report.setStatus(ReportStatus.PENDING.name());
        reportMapper.insert(report);
        return Result.success(null);
    }

    @Data
    public static class ReportRequest {
        /** CAMPAIGN/COMMENT/USER */
        private String targetType;
        private Long targetId;
        private String reason;
    }
}