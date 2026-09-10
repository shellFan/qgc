package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.content.entity.Comment;

/**
 * 后台-评论管理Service
 */
public interface AdminCommentService {

    /**
     * 分页查询评论
     */
    IPage<Comment> listComments(Page<Comment> page, String targetType, Integer status);

    /**
     * 更新评论状态
     */
    void updateStatus(Long id, Integer status, Long adminId);
}