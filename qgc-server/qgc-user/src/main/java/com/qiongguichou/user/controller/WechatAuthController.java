package com.qiongguichou.user.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.user.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信OAuth认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class WechatAuthController {

    private final WechatAuthService wechatAuthService;

    public WechatAuthController(WechatAuthService wechatAuthService) {
        this.wechatAuthService = wechatAuthService;
    }

    /**
     * 获取微信授权URL
     *
     * @param redirectUri 回调地址(需在微信后台配置)
     * @return 授权URL
     */
    @GetMapping("/authorize-url")
    public Result<String> getAuthorizeUrl(@RequestParam String redirectUri) {
        String state = String.valueOf(System.currentTimeMillis());
        String url = wechatAuthService.buildAuthorizationUrl(redirectUri, state);
        return Result.success(url);
    }

    /**
     * 微信OAuth回调接口
     * 微信授权后会回调此接口，携带code和state参数
     *
     * @param code  授权码
     * @param state 状态参数
     * @return 登录结果(包含token和用户信息)
     */
    @GetMapping("/callback")
    public Result<Map<String, Object>> callback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("微信OAuth回调, code={}, state={}", code, state);
        Map<String, Object> loginResult = wechatAuthService.loginByCode(code);
        return Result.success(loginResult);
    }

    /**
     * 通过授权码直接登录(H5页面使用)
     * 适用于前端获取code后直接调用此接口完成登录
     *
     * @param code 授权码
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String code) {
        log.info("微信登录, code={}", code);
        Map<String, Object> loginResult = wechatAuthService.loginByCode(code);
        return Result.success(loginResult);
    }

    /**
     * 获取微信JS-SDK签名配置
     *
     * @param url 当前页面URL
     * @return 签名配置
     */
    @GetMapping("/jssdk-config")
    public Result<Map<String, String>> getJsSdkConfig(@RequestParam String url) {
        Map<String, String> config = wechatAuthService.getJsSdkConfig(url);
        return Result.success(config);
    }
}