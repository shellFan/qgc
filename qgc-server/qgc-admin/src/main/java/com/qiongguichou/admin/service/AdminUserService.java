package com.qiongguichou.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.user.entity.User;

/**
 * 后台-用户管理服务
 */
public interface AdminUserService {

    /**
     * 分页查询用户列表
     */
    IPage<User> listUsers(Page<User> page, String keyword, String status);

    /**
     * 禁用用户
     */
    void disableUser(Long userId);

    /**
     * 启用用户
     */
    void enableUser(Long userId);

    /**
     * 获取用户详情
     */
    User getUserDetail(Long userId);
}