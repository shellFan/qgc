package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminCampaignService;
import com.qiongguichou.campaign.entity.Campaign;
import com.qiongguichou.campaign.mapper.CampaignMapper;
import com.qiongguichou.common.enums.CampaignStatus;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.content.entity.Comment;
import com.qiongguichou.content.mapper.CommentMapper;
import com.qiongguichou.content.mapper.ReportMapper;
import com.qiongguichou.content.entity.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台-审核中心Controller
 */
@RestController
@RequestMapping("/admin/api/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final CampaignMapper campaignMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final AdminCampaignService adminCampaignService;

    /**
     * 待审核筹款列表
     */
    @GetMapping("/campaigns")
    public Result<IPage<Campaign>> getPendingCampaigns(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Campaign> page = new Page<>(pageNum, pageSize);
        IPage<Campaign> result = campaignMapper.selectPage(page,
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, CampaignStatus.PENDING_REVIEW.name())
                        .orderByAsc(Campaign::getCreateTime));
        return Result.success(result);
    }

    /**
     * 审核筹款(通过/拒绝)
     */
    @PostMapping("/campaign/{id}")
    public Result<Void> reviewCampaign(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        boolean approved = Boolean.parseBoolean(String.valueOf(body.get("approved")));
        String rejectReason = (String) body.get("rejectReason");
        if (approved) {
            adminCampaignService.approve(id, null);
        } else {
            adminCampaignService.reject(id, rejectReason != null ? rejectReason : "审核拒绝", null);
        }
        return Result.success();
    }

    /**
     * 待审核评论列表
     */
    @GetMapping("/comments")
    public Result<IPage<Comment>> getPendingComments(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        IPage<Comment> result = commentMapper.selectPage(page,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getStatus, 0)
                        .orderByAsc(Comment::getCreateTime));
        return Result.success(result);
    }

    /**
     * 审核评论(通过/隐藏/删除)
     */
    @PostMapping("/comment/{id}")
    public Result<Void> reviewComment(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        commentMapper.updateById(comment);
        return Result.success();
    }

    /**
     * 举报列表
     */
    @GetMapping("/reports")
    public Result<IPage<Report>> getReports(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "PENDING") String status) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        IPage<Report> result = reportMapper.selectPage(page,
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getStatus, status)
                        .orderByDesc(Report::getCreateTime));
        return Result.success(result);
    }

    /**
     * 处理举报
     */
    @PostMapping("/report/{id}")
    public Result<Void> handleReport(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> body) {
        String status = (String) body.get("status");
        String remark = (String) body.get("remark");
        Report report = new Report();
        report.setId(id);
        report.setStatus(status);
        report.setHandleRemark(remark);
        reportMapper.updateById(report);
        return Result.success();
    }
}