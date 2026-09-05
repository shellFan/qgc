package com.qiongguichou.content.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.content.entity.Notification;
import com.qiongguichou.content.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public Result<IPage<Notification>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = UserContext.getUserId();
        return Result.success(notificationService.getUserNotifications(userId, page, size));
    }

    /**
     * 标记已读
     */
    @PostMapping("/read/{id}")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        notificationService.markRead(userId, id);
        return Result.success(null);
    }

    /**
     * 全部标记已读
     */
    @PostMapping("/read/all")
    public Result<Void> markAllRead() {
        Long userId = UserContext.getUserId();
        notificationService.markAllRead(userId);
        return Result.success(null);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread/count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = UserContext.getUserId();
        int count = notificationService.getUnreadCount(userId);
        Map<String, Integer> map = new HashMap<>();
        map.put("count", count);
        return Result.success(map);
    }
}