package com.qiongguichou.admin.controller;

import com.qiongguichou.admin.entity.Role;
import com.qiongguichou.admin.service.AdminRoleService;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台-角色管理Controller
 */
@RestController
@RequestMapping("/admin/api/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 获取所有角色
     */
    @GetMapping
    public Result<List<Role>> list() {
        return Result.success(adminRoleService.listRoles());
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<Role> detail(@PathVariable Long id) {
        return Result.success(adminRoleService.getRole(id));
    }

    /**
     * 更新角色信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String roleName = body.get("roleName");
        String description = body.get("description");
        adminRoleService.updateRole(id, roleName, description);
        return Result.success();
    }
}