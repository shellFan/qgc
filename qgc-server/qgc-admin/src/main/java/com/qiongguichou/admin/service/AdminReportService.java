package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.content.entity.Report;

/**
 * 后台-举报管理Service
 */
public interface AdminReportService {

    /**
     * 分页查询举报
     */
    IPage<Report> page(int pageNum, int pageSize, String status, String targetType, String reasonType);

    /**
     * 获取举报详情
     */
    Report getDetail(Long id);

    /**
     * 处理举报(确认/忽略)
     */
    void handle(Long id, Long adminId, String status, String handleRemark);
}