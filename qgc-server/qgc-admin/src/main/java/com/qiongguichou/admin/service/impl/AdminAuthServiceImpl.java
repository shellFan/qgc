package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.mapper.AdminMapper;
import com.qiongguichou.admin.service.AdminAuthService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.core.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String username, String password, String ip) {
        // 查询管理员
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );
        if (admin == null) {
            throw new BusinessException(ErrorCode.ADMIN_UNAUTHORIZED, "用户名或密码错误");
        }

        // 校验密码（BCrypt）
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            throw new BusinessException(ErrorCode.ADMIN_UNAUTHORIZED, "用户名或密码错误");
        }

        // 校验状态
        if (admin.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN, "账号已被禁用");
        }

        // 更新登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        adminMapper.updateById(admin);

        // 生成管理员Token
        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("nickname", admin.getNickname());
        return result;
    }

    @Override
    public Admin getCurrentAdmin(Long adminId) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.ADMIN_UNAUTHORIZED, "管理员不存在");
        }
        if (admin.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ADMIN_FORBIDDEN, "账号已被禁用");
        }
        return admin;
    }
}