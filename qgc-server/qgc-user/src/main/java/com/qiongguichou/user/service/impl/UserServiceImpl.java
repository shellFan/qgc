package com.qiongguichou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.mapper.UserMapper;
import com.qiongguichou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User createOrUpdate(User user) {
        User existing = getByOpenid(user.getOpenid());
        if (existing != null) {
            // 更新已有用户
            user.setId(existing.getId());
            userMapper.updateById(user);
            return user;
        }
        // 新用户
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getSubscribe() == null) {
            user.setSubscribe(0);
        }
        if (user.getSex() == null) {
            user.setSex(0);
        }
        userMapper.insert(user);
        return user;
    }

    @Override
    public boolean update(User user) {
        return userMapper.updateById(user) > 0;
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public IPage<User> getMyCampaigns(Long userId, int pageNum, int pageSize) {
        // 我的筹款已通过CampaignController.getMyCampaigns实现，此方法保留兼容
        return new Page<>(pageNum, pageSize, 0);
    }

    @Override
    public IPage<User> getMySupports(Long userId, int pageNum, int pageSize) {
        // 我的支持已通过H5 API直接查询，此方法保留兼容
        return new Page<>(pageNum, pageSize, 0);
    }

    @Override
    public void checkUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }
    }
}