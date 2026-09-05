package com.qiongguichou.admin.controller;

import com.qiongguichou.admin.entity.Admin;
import com.qiongguichou.admin.service.AdminAuthService;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 管理员认证控制器
 */
@RestController
@RequestMapping("/admin/api/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params,
                                              HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || password == null) {
            return Result.error(ErrorCode.PARAM_ERROR.getCode(), "用户名和密码不能为空");
        }
        String ip = request.getRemoteAddr();
        Map<String, Object> data = adminAuthService.login(username, password, ip);
        return Result.success(data);
    }

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    public Result<Admin> getInfo(@RequestAttribute("adminId") Long adminId) {
        Admin admin = adminAuthService.getCurrentAdmin(adminId);
        // 不返回密码
        admin.setPassword(null);
        return Result.success(admin);
    }
}