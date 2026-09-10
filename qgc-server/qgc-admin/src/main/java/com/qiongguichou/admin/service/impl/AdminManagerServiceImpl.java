package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.entity.AdminLog;
import com.qiongguichou.admin.entity.AdminRole;
import com.qiongguichou.admin.mapper.AdminLogMapper;
import com.qiongguichou.admin.mapper.AdminMapper;
import com.qiongguichou.admin.mapper.AdminRoleMapper;
import com.qiongguichou.admin.service.AdminManagerService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 后台-管理员管理Service实现
 */
@Service
@RequiredArgsConstructor
public class AdminManagerServiceImpl implements AdminManagerService {

    private final AdminMapper adminMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public IPage<Admin> listManagers(Page<Admin> page) {
        return adminMapper.selectPage(page,
                new LambdaQueryWrapper<Admin>().orderByDesc(Admin::getCreateTime));
    }

    @Override
    @Transactional
    public Admin createManager(String username, String password, String nickname, List<Long> roleIds, Long operatorId) {
        // 检查用户名是否已存在
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        admin.setNickname(nickname);
        admin.setStatus(1);
        adminMapper.insert(admin);

        // 分配角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(admin.getId());
                adminRole.setRoleId(roleId);
                adminRoleMapper.insert(adminRole);
            }
        }

        AdminLog log = new AdminLog();
        log.setAdminId(operatorId);
        log.setAction("CREATE");
        log.setTargetType("ADMIN");
        log.setTargetId(String.valueOf(admin.getId()));
        log.setAfterData("username=" + username);
        adminLogMapper.insert(log);

        return admin;
    }

    @Override
    @Transactional
    public void updateManager(Long id, String nickname, List<Long> roleIds, Integer status, Long operatorId) {
        Admin admin = getAdmin(id);

        if (nickname != null) {
            admin.setNickname(nickname);
        }
        if (status != null) {
            admin.setStatus(status);
        }
        adminMapper.updateById(admin);

        // 更新角色
        if (roleIds != null) {
            adminRoleMapper.delete(
                    new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getAdminId, id));
            for (Long roleId : roleIds) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(id);
                adminRole.setRoleId(roleId);
                adminRoleMapper.insert(adminRole);
            }
        }

        AdminLog log = new AdminLog();
        log.setAdminId(operatorId);
        log.setAction("UPDATE");
        log.setTargetType("ADMIN");
        log.setTargetId(String.valueOf(id));
        adminLogMapper.insert(log);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        Admin admin = getAdmin(id);

        // 校验旧密码
        if (!BCrypt.checkpw(oldPassword, admin.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "旧密码错误");
        }

        admin.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        admin.setMustChangePassword(0);
        adminMapper.updateById(admin);
    }

    @Override
    public void deleteManager(Long id, Long operatorId) {
        Admin admin = getAdmin(id);
        // 逻辑删除
        adminMapper.deleteById(id);

        AdminLog log = new AdminLog();
        log.setAdminId(operatorId);
        log.setAction("DELETE");
        log.setTargetType("ADMIN");
        log.setTargetId(String.valueOf(id));
        adminLogMapper.insert(log);
    }

    @Override
    public void toggleManager(Long id, Long operatorId) {
        Admin admin = getAdmin(id);
        admin.setStatus(admin.getStatus() == 1 ? 2 : 1);
        adminMapper.updateById(admin);

        AdminLog log = new AdminLog();
        log.setAdminId(operatorId);
        log.setAction("TOGGLE");
        log.setTargetType("ADMIN");
        log.setTargetId(String.valueOf(id));
        log.setAfterData("status=" + admin.getStatus());
        adminLogMapper.insert(log);
    }

    private Admin getAdmin(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "管理员不存在");
        }
        return admin;
    }
}