package com.qiongguichou.content.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.content.entity.Notification;

/**
 * 通知服务
 */
public interface NotificationService {

    /**
     * 发送通知
     * @param userId 接收用户ID
     * @param type 类型: SUPPORT/SUCCESS/EXPIRE/COMMENT/LIKE/WITHDRAW/REVIEW/ADMIN
     * @param title 标题
     * @param content 内容
     * @param relatedId 关联ID
     * @param relatedType 关联类型
     */
    void sendNotification(Long userId, String type, String title, String content, Long relatedId, String relatedType);

    /**
     * 获取用户通知列表
     */
    IPage<Notification> getUserNotifications(Long userId, int page, int size);

    /**
     * 标记已读
     */
    void markRead(Long userId, Long notificationId);

    /**
     * 全部标记已读
     */
    void markAllRead(Long userId);

    /**
     * 获取未读数量
     */
    int getUnreadCount(Long userId);
}