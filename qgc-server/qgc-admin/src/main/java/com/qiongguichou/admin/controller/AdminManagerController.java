package com.qiongguichou.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.service.AdminManagerService;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台-管理员管理Controller
 */
@RestController
@RequestMapping("/admin/api/managers")
@RequiredArgsConstructor
public class AdminManagerController {

    private final AdminManagerService adminManagerService;

    /**
     * 分页列表
     */
    @GetMapping
    public Result<IPage<Admin>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Admin> p = new Page<>(page, size);
        return Result.success(adminManagerService.listManagers(p));
    }

    /**
     * 创建管理员
     */
    @PostMapping
    public Result<Admin> create(
            @RequestBody Map<String, Object> body,
            @RequestAttribute("adminId") Long adminId) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String nickname = (String) body.get("nickname");
        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) body.get("roleIds");
        return Result.success(adminManagerService.createManager(username, password, nickname, roleIds, adminId));
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestAttribute("adminId") Long adminId) {
        String nickname = (String) body.get("nickname");
        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) body.get("roleIds");
        Integer status = body.get("status") != null ? Integer.valueOf(body.get("status").toString()) : null;
        adminManagerService.updateManager(id, nickname, roleIds, status, adminId);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        adminManagerService.changePassword(id, oldPassword, newPassword);
        return Result.success();
    }

    /**
     * 删除管理员(逻辑删除)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestAttribute("adminId") Long adminId) {
        adminManagerService.deleteManager(id, adminId);
        return Result.success();
    }

    /**
     * 启用/禁用切换
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> toggle(
            @PathVariable Long id,
            @RequestAttribute("adminId") Long adminId) {
        adminManagerService.toggleManager(id, adminId);
        return Result.success();
    }
}