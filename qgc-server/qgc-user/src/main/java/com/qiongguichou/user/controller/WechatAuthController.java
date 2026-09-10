package com.qiongguichou.user.controller;

import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.common.result.Result;
import com.qiongguichou.core.config.RedisService;
import com.qiongguichou.user.service.WechatAuthService;
import com.qiongguichou.user.service.MiniProgramAuthService;
import com.qiongguichou.user.dto.MiniProgramLoginRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信OAuth认证控制器
 * 
 * OAuth State安全：
 * - 使用SecureRandom生成state
 * - Redis存储state，TTL 5分钟
 * - 一次性使用，验证后删除
 * - 同state第二次请求拒绝
 * 
 * Open Redirect防护：
 * - redirectUri只允许本站相对路径或配置的允许域名
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class WechatAuthController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String STATE_KEY_PREFIX = "qgc:oauth:state:";
    private static final long STATE_TTL_MINUTES = 5;

    /** 允许的回调域名(逗号分隔)，从配置读取 */
    @Value("${qgc.wechat.allowed-domains:}")
    private String allowedDomains;

    private final WechatAuthService wechatAuthService;
    private final MiniProgramAuthService miniProgramAuthService;
    private final RedisService redisService;

    public WechatAuthController(WechatAuthService wechatAuthService, MiniProgramAuthService miniProgramAuthService,
                                RedisService redisService) {
        this.wechatAuthService = wechatAuthService;
        this.miniProgramAuthService = miniProgramAuthService;
        this.redisService = redisService;
    }

    @PostMapping("/miniapp/login")
    public Result<Map<String, Object>> miniProgramLogin(@Valid @RequestBody MiniProgramLoginRequest request) {
        return Result.success(miniProgramAuthService.login(
                request.getCode(), request.getNickname(), request.getAvatar()));
    }

    /**
     * 获取微信授权URL
     *
     * @param redirectUri 回调后跳转地址(只允许本站路径或允许域名)
     * @return 授权URL + state
     */
    @GetMapping("/authorize-url")
    public Result<Map<String, String>> getAuthorizeUrl(@RequestParam String redirectUri) {
        // Open Redirect防护：校验redirectUri
        validateRedirectUri(redirectUri);

        // 生成安全state
        byte[] stateBytes = new byte[24];
        SECURE_RANDOM.nextBytes(stateBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);

        // Redis存储state，TTL 5分钟
        String stateKey = STATE_KEY_PREFIX + state;
        // value存储redirectUri，回调时需要
        redisService.set(stateKey, redirectUri, STATE_TTL_MINUTES, TimeUnit.MINUTES);

        // 构造微信OAuth回调地址(固定为本站callback端点)
        String callbackUri = buildCallbackUri();
        String url = wechatAuthService.buildAuthorizationUrl(callbackUri, state);

        Map<String, String> result = new HashMap<>();
        result.put("authorizeUrl", url);
        result.put("state", state);
        return Result.success(result);
    }

    /**
     * 微信OAuth回调接口
     * 微信授权后会回调此接口，携带code和state参数
     *
     * @param code  授权码
     * @param state 状态参数(防CSRF，一次性)
     * @return 登录结果(包含token和用户信息)
     */
    @GetMapping("/callback")
    public Result<Map<String, Object>> callback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        log.info("微信OAuth回调, code={}, state={}", code, state);

        // State安全验证
        if (state == null || state.isEmpty()) {
            log.warn("OAuth回调缺少state参数");
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL);
        }

        String stateKey = STATE_KEY_PREFIX + state;
        String savedRedirect = redisService.get(stateKey);

        if (savedRedirect == null) {
            // state不存在或已过期或已使用
            log.warn("OAuth state无效或已过期: state={}", state);
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL);
        }

        // 一次性使用：验证后立即删除
        redisService.delete(stateKey);

        // 执行登录
        Map<String, Object> loginResult = wechatAuthService.loginByCode(code);

        // 将redirectUri放入结果，前端负责跳转
        loginResult.put("redirectUri", savedRedirect);
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

    /**
     * Open Redirect防护
     * 只允许：1)相对路径 2)配置的允许域名
     */
    private void validateRedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 允许相对路径(以/开头，不含协议头)
        if (redirectUri.startsWith("/") && !redirectUri.startsWith("//")) {
            return;
        }
        // 允许配置的域名
        if (allowedDomains != null && !allowedDomains.isEmpty()) {
            for (String domain : allowedDomains.split(",")) {
                String trimmed = domain.trim();
                if (!trimmed.isEmpty() && redirectUri.startsWith(trimmed)) {
                    return;
                }
            }
        }
        log.warn("Open Redirect防护: 拒绝redirectUri={}", redirectUri);
        throw new BusinessException(ErrorCode.PARAM_ERROR);
    }

    /**
     * 构造微信OAuth回调URI(固定为本站callback端点)
     */
    @Value("${qgc.wechat.callback-base-url:}")
    private String callbackBaseUrl;

    private String buildCallbackUri() {
        if (callbackBaseUrl != null && !callbackBaseUrl.isEmpty()) {
            return callbackBaseUrl + "/api/auth/callback";
        }
        // 默认使用请求域名(需在部署时配置)
        return "/api/auth/callback";
    }
}
