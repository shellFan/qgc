package com.qiongguichou.core.service;

/**
 * 用户状态检查接口
 * RC5: JWT有效但用户已封禁时仍需检查
 *
 * 实现类在qgc-user模块，通过依赖注入提供给UserAuthInterceptor
 */
public interface UserStatusChecker {

    /**
     * 检查用户是否正常(未封禁)
     *
     * @param userId 用户ID
     * @return true=正常, false=已封禁
     */
    boolean isUserActive(Long userId);
}