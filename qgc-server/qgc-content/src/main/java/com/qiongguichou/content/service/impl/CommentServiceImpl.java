package com.qiongguichou.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.util.XssUtil;
import com.qiongguichou.content.entity.Comment;
import com.qiongguichou.content.mapper.CommentMapper;
import com.qiongguichou.content.service.CommentService;
import com.qiongguichou.content.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public Comment addComment(Long userId, Long targetId, String targetType, String content) {
        // XSS清理
        content = XssUtil.clean(content);

        // 敏感词检测
        if (sensitiveWordService.containsSensitiveWord(content)) {
            throw new BusinessException(ErrorCode.SENSITIVE_WORD_DETECTED);
        }

        // 长度限制
        if (content.length() > 500) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评论最多500字");
        }

        Comment comment = new Comment();
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setStatus(1); // 1正常
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    public IPage<Comment> getTargetComments(Long targetId, String targetType, int page, int size) {
        Page<Comment> p = new Page<>(page, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getTargetType, targetType)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreateTime);
        return commentMapper.selectPage(p, wrapper);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        // 软删除：状态改为3(删除)
        comment.setStatus(3);
        commentMapper.updateById(comment);
    }
}