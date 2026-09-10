package com.qiongguichou.admin.service;

import com.qiongguichou.admin.entity.Role;

import java.util.List;

/**
 * 后台-角色管理Service
 */
public interface AdminRoleService {

    /**
     * 获取所有角色
     */
    List<Role> listRoles();

    /**
     * 获取角色详情
     */
    Role getRole(Long id);

    /**
     * 更新角色信息
     */
    void updateRole(Long id, String roleName, String description);
}