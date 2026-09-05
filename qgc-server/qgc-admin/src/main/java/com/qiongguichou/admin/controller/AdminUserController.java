package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminUserService;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台-用户管理控制器
 */
@RestController
@RequestMapping("/admin/api/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<IPage<User>> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String status) {
        Page<User> pageParam = new Page<>(page, size);
        return Result.success(adminUserService.listUsers(pageParam, keyword, status));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    /**
     * 禁用用户
     */
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        adminUserService.disableUser(id);
        return Result.success();
    }

    /**
     * 启用用户
     */
    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        adminUserService.enableUser(id);
        return Result.success();
    }
}