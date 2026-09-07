package com.qiongguichou.user.service;

import com.qiongguichou.core.service.UserStatusChecker;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户状态检查实现
 * RC5: 封禁用户即使JWT有效也不能访问
 *
 * User实体status字段: 1=正常, 2=封禁
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusCheckerImpl implements UserStatusChecker {

    private final UserMapper userMapper;

    @Override
    public boolean isUserActive(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        // status: 1=正常, 2=封禁
        return user.getStatus() != null && user.getStatus() == 1;
    }
}