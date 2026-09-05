package com.qiongguichou.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.admin.service.AdminUserService;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 后台-用户管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;

    @Override
    public IPage<User> listUsers(Page<User> page, String keyword, String status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getOpenid, keyword));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public void disableUser(Long userId) {
        User user = getUser(userId);
        user.setStatus(2); // 2=封禁
        userMapper.updateById(user);
        log.info("用户被禁用: userId={}", userId);
    }

    @Override
    public void enableUser(Long userId) {
        User user = getUser(userId);
        user.setStatus(1); // 1=正常
        userMapper.updateById(user);
        log.info("用户被启用: userId={}", userId);
    }

    @Override
    public User getUserDetail(Long userId) {
        return getUser(userId);
    }

    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}