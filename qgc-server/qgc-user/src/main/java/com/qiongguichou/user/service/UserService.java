package com.qiongguichou.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiongguichou.user.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据ID查询用户
     */
    User getById(Long id);

    /**
     * 根据openid查询用户
     */
    User getByOpenid(String openid);

    /**
     * 创建或更新用户(微信授权后)
     */
    User createOrUpdate(User user);

    /**
     * 更新用户信息
     */
    boolean update(User user);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);

    /**
     * 获取我的筹款列表(分页)
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 筹款分页数据
     */
    IPage<User> getMyCampaigns(Long userId, int pageNum, int pageSize);

    /**
     * 获取我的投喂列表(分页)
     *
     * @param userId   用户ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 投喂分页数据
     */
    IPage<User> getMySupports(Long userId, int pageNum, int pageSize);

    /**
     * 检查用户状态是否正常
     */
    void checkUserStatus(Long userId);
}