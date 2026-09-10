package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.entity.Admin;

import java.util.List;

/**
 * 后台-管理员管理Service
 */
public interface AdminManagerService {

    /**
     * 分页查询管理员列表
     */
    IPage<Admin> listManagers(Page<Admin> page);

    /**
     * 创建管理员
     */
    Admin createManager(String username, String password, String nickname, List<Long> roleIds, Long operatorId);

    /**
     * 更新管理员信息
     */
    void updateManager(Long id, String nickname, List<Long> roleIds, Integer status, Long operatorId);

    /**
     * 修改密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 删除管理员(逻辑删除)
     */
    void deleteManager(Long id, Long operatorId);

    /**
     * 启用/禁用切换
     */
    void toggleManager(Long id, Long operatorId);
}