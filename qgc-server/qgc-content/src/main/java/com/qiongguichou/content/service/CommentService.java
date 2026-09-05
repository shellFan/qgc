package com.qiongguichou.content.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.content.entity.Comment;

/**
 * 评论服务
 */
public interface CommentService {

    /**
     * 发表评论
     * @param userId 评论用户ID
     * @param targetId 目标ID(筹款ID或返图ID)
     * @param targetType 目标类型: CAMPAIGN/PROOF
     * @param content 评论内容
     */
    Comment addComment(Long userId, Long targetId, String targetType, String content);

    /**
     * 获取目标评论列表
     */
    IPage<Comment> getTargetComments(Long targetId, String targetType, int page, int size);

    /**
     * 删除评论(仅本人或管理员)
     */
    void deleteComment(Long userId, Long commentId);
}