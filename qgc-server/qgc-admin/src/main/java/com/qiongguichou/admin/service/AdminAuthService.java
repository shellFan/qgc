package com.qiongguichou.admin.service;

import com.qiongguichou.admin.entity.Admin;

import java.util.Map;

/**
 * 管理员认证服务
 */
public interface AdminAuthService {

    /**
     * 管理员登录
     */
    Map<String, Object> login(String username, String password, String ip);

    /**
     * 获取当前管理员信息
     */
    Admin getCurrentAdmin(Long adminId);
}