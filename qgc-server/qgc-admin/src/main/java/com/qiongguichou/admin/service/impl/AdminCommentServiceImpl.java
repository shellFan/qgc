package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.service.AdminCommentService;
import com.qiongguichou.content.entity.Comment;
import com.qiongguichou.content.mapper.CommentMapper;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台-评论管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminCommentServiceImpl implements AdminCommentService {

    private final CommentMapper commentMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<Comment> listComments(Page<Comment> page, String targetType, Integer status) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(Comment::getTargetType, targetType);
        }
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        return commentMapper.selectPage(page, wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status, Long adminId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        comment.setStatus(status);
        commentMapper.updateById(comment);

        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAction("UPDATE_STATUS");
        log.setTargetType("COMMENT");
        log.setTargetId(String.valueOf(id));
        log.setAfterData("status=" + status);
        adminLogMapper.insert(log);
    }
}