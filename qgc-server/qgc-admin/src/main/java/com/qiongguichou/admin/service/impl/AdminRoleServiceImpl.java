package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.admin.entity.Role;
import com.qiongguichou.admin.mapper.RoleMapper;
import com.qiongguichou.admin.service.AdminRoleService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台-角色管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl implements AdminRoleService {

    private final RoleMapper roleMapper;

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getId));
    }

    @Override
    public Role getRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return role;
    }

    @Override
    public void updateRole(Long id, String roleName, String description) {
        Role role = getRole(id);
        if (roleName != null) {
            role.setRoleName(roleName);
        }
        if (description != null) {
            role.setDescription(description);
        }
        roleMapper.updateById(role);
    }
}