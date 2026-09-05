package com.qiongguichou.content.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.content.entity.Comment;
import com.qiongguichou.content.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Comment> addComment(@RequestBody CommentRequest request) {
        Long userId = UserContext.getUserId();
        Comment comment = commentService.addComment(
                userId, request.getTargetId(), request.getTargetType(), request.getContent());
        return Result.success(comment);
    }

    /**
     * 获取目标评论列表(筹款或返图)
     */
    @GetMapping("/list/{targetId}")
    public Result<IPage<Comment>> getTargetComments(
            @PathVariable Long targetId,
            @RequestParam String targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.getTargetComments(targetId, targetType, page, size));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        commentService.deleteComment(userId, commentId);
        return Result.success(null);
    }

    @lombok.Data
    public static class CommentRequest {
        private Long targetId;
        private String targetType;
        private String content;
    }
}