package com.qiongguichou.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.common.util.UserContext;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(com.qiongguichou.common.result.ErrorCode.USER_NOT_FOUND);
        }
        // 清除敏感信息
        user.setDeleted(null);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        Long userId = UserContext.getUserId();
        user.setId(userId);
        // 不允许通过此接口修改以下字段
        user.setOpenid(null);
        user.setUnionid(null);
        user.setStatus(null);
        user.setDeleted(null);
        userService.update(user);
        return Result.success();
    }

    /**
     * 获取我的筹款列表
     */
    @GetMapping("/campaigns")
    public Result<IPage<User>> getMyCampaigns(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        IPage<User> page = userService.getMyCampaigns(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 获取我的投喂列表
     */
    @GetMapping("/supports")
    public Result<IPage<User>> getMySupports(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        IPage<User> page = userService.getMySupports(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check")
    public Result<Boolean> checkLogin() {
        Long userId = UserContext.getUserId();
        return Result.success(userId != null);
    }
}