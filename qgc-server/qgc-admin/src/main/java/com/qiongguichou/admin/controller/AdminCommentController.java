package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminCommentService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.content.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台-评论管理Controller
 */
@RestController
@RequestMapping("/admin/api/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<Comment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Integer status) {
        Page<Comment> p = new Page<>(page, size);
        return Result.success(adminCommentService.listComments(p, targetType, status));
    }

    /**
     * 更新状态(1正常/2隐藏/3删除)
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestAttribute("adminId") Long adminId) {
        Integer status = Integer.valueOf(body.get("status").toString());
        adminCommentService.updateStatus(id, status, adminId);
        return Result.success();
    }
}