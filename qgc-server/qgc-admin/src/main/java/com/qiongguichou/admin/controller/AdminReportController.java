package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.admin.service.AdminReportService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.content.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台-举报管理Controller
 */
@RestController
@RequestMapping("/admin/api/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    /**
     * 分页查询举报
     */
    @GetMapping
    public Result<IPage<Report>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String reasonType) {
        return Result.success(adminReportService.page(pageNum, pageSize, status, targetType, reasonType));
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    public Result<Report> detail(@PathVariable Long id) {
        return Result.success(adminReportService.getDetail(id));
    }

    /**
     * 处理举报(确认/忽略)
     */
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id,
                               @RequestAttribute("adminId") Long adminId,
                               @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String handleRemark = body.get("handleRemark");
        adminReportService.handle(id, adminId, status, handleRemark);
        return Result.success();
    }
}