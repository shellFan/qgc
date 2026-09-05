package com.qiongguichou.user.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.config.JwtUtil;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 开发模式登录控制器
 * 仅在 qgc.auth.mock-enabled=true 时生效
 * 用于本地开发调试, 不需要真实的微信OAuth流程
 */
@Slf4j
@RestController
@RequestMapping("/dev")
@ConditionalOnProperty(name = "qgc.auth.mock-enabled", havingValue = "true")
public class DevLoginController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${qgc.auth.mock-user-id:1}")
    private Long mockUserId;

    public DevLoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 开发模式登录
     * 直接使用指定用户ID生成Token, 跳过微信OAuth
     *
     * @param userId 可选, 指定登录的用户ID, 默认使用配置的mock-user-id
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> devLogin(
            @RequestParam(required = false) Long userId) {
        if (userId == null) {
            userId = mockUserId;
        }

        User user = userService.getById(userId);
        if (user == null) {
            log.warn("DEV登录失败: 用户ID={} 不存在", userId);
            return Result.error(com.qiongguichou.common.result.ErrorCode.USER_NOT_FOUND);
        }

        // 生成JWT Token
        String token = jwtUtil.generateUserToken(user.getId(), user.getOpenid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("openid", user.getOpenid());

        log.info("DEV登录成功: userId={}, nickname={}", user.getId(), user.getNickname());
        return Result.success(result);
    }

    /**
     * 通过openid登录(开发模式)
     */
    @PostMapping("/login-by-openid")
    public Result<Map<String, Object>> devLoginByOpenid(@RequestParam String openid) {
        User user = userService.getByOpenid(openid);
        if (user == null) {
            log.warn("DEV登录失败: openid={} 不存在", openid);
            return Result.error(com.qiongguichou.common.result.ErrorCode.USER_NOT_FOUND);
        }

        String token = jwtUtil.generateUserToken(user.getId(), user.getOpenid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());

        log.info("DEV登录成功(openid): userId={}, nickname={}", user.getId(), user.getNickname());
        return Result.success(result);
    }
}