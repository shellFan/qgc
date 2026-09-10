package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminReportService;
import com.qiongguichou.content.entity.Report;
import com.qiongguichou.content.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 后台-举报管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportMapper reportMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<Report> page(int pageNum, int pageSize, String status, String targetType, String reasonType) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Report::getStatus, status);
        }
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(Report::getTargetType, targetType);
        }
        if (StringUtils.hasText(reasonType)) {
            wrapper.eq(Report::getReasonType, reasonType);
        }
        wrapper.orderByDesc(Report::getCreateTime);
        return reportMapper.selectPage(page, wrapper);
    }

    @Override
    public Report getDetail(Long id) {
        return reportMapper.selectById(id);
    }

    @Override
    @Transactional
    public void handle(Long id, Long adminId, String status, String handleRemark) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        String beforeStatus = report.getStatus();
        report.setStatus(status);
        report.setHandlerId(adminId);
        report.setHandleTime(LocalDateTime.now());
        report.setHandleRemark(handleRemark);
        reportMapper.updateById(report);

        // 记录操作日志
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("HANDLE_REPORT");
        log.setTargetType("REPORT");
        log.setTargetId(String.valueOf(id));
        log.setBeforeData("{\"status\":\"" + beforeStatus + "\"}");
        log.setAfterData("{\"status\":\"" + status + "\",\"handleRemark\":\"" + handleRemark + "\"}");
        adminLogMapper.insert(log);
    }
}