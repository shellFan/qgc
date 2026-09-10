package com.qiongguichou.user.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.config.JwtUtil;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 快速登录控制器
 * 不依赖微信OAuth，用户输入昵称即可登录/注册
 * 生产环境可用
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class QuickLoginController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public QuickLoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 快速登录
     * 设备快速登录：设备首次访问创建内部用户，后续用设备身份恢复该用户。
     * 昵称只是展示字段，不能作为认证凭据。
     */
    @PostMapping("/quick-login")
    public Result<Map<String, Object>> quickLogin(@RequestBody Map<String, String> params) {
        String nickname = params.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.error(com.qiongguichou.common.result.ErrorCode.PARAM_ERROR, "昵称不能为空");
        }
        if (nickname.length() > 20) {
            return Result.error(com.qiongguichou.common.result.ErrorCode.PARAM_ERROR, "昵称最长20个字符");
        }

        String deviceId = params.get("deviceId");
        if (deviceId == null || deviceId.trim().isEmpty() || deviceId.length() > 128) {
            return Result.error(com.qiongguichou.common.result.ErrorCode.PARAM_ERROR, "设备标识无效，请刷新后重试");
        }

        // 设备身份是随机生成并由客户端持久化的内部标识；昵称不参与身份查找。
        String pseudoOpenid = "quick_device_" + deviceId.trim();

        User user = userService.getByOpenid(pseudoOpenid);
        if (user == null) {
            // 注册新用户
            user = new User();
            user.setOpenid(pseudoOpenid);
            user.setNickname(nickname.trim());
            user.setAvatar("");
            user.setStatus(1);
            user.setSubscribe(0);
            user.setSex(0);
            user = userService.createOrUpdate(user);
            log.info("快速注册: userId={}, nickname={}", user.getId(), user.getNickname());
        } else {
            // 更新登录时间
            userService.updateLastLoginTime(user.getId());
            log.info("快速登录: userId={}, nickname={}", user.getId(), user.getNickname());
        }

        // 生成JWT Token
        String token = jwtUtil.generateUserToken(user.getId(), user.getOpenid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("deviceId", deviceId.trim());

        return Result.success(result);
    }
}
