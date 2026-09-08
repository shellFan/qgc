package com.qiongguichou.user.service.impl;

import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.core.config.JwtUtil;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.service.UserService;
import com.qiongguichou.user.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信OAuth认证服务实现类
 * 支持Mock模式：qgc.wechat.mock-enabled=true时使用Mock实现
 */
@Slf4j
@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    @Value("${qgc.wechat.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${qgc.wechat.mp.appId:mock_appid}")
    private String appId;

    @Autowired(required = false)
    private WxMpService wxMpService;

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public WechatAuthServiceImpl(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        if (mockEnabled) {
            log.info("Mock模式: 构造模拟OAuth URL");
            return redirectUri + (redirectUri.contains("?") ? "&" : "?") + "code=mock_code&state=" + state;
        }
        return wxMpService.getOAuth2Service().buildAuthorizationUrl(
                redirectUri,
                "snsapi_userinfo",
                state
        );
    }

    @Override
    public Map<String, Object> loginByCode(String code) {
        String openid;
        String unionid = null;

        if (mockEnabled) {
            // Mock模式：生成模拟openid
            openid = "mock_openid_" + System.currentTimeMillis() % 10000;
            log.info("Mock模式: 使用模拟openid={}, code={}", openid, code);
        } else {
            try {
                WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
                openid = accessToken.getOpenId();
                unionid = accessToken.getUnionId();
                log.info("微信OAuth授权成功, openid={}", openid);
            } catch (WxErrorException e) {
                log.error("微信OAuth授权失败: {}", e.getMessage(), e);
                throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL);
            }
        }

        // 创建或更新本地用户
        User user = new User();
        user.setOpenid(openid);
        user.setUnionid(unionid);
        user.setNickname("微信用户");
        user.setAvatar("");
        user.setSex(0);
        user.setSubscribe(0);

        user = userService.createOrUpdate(user);
        userService.updateLastLoginTime(user.getId());

        // 生成JWT Token
        String token = jwtUtil.generateUserToken(user.getId(), user.getOpenid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        return result;
    }

    @Override
    public Map<String, String> getJsSdkConfig(String url) {
        if (mockEnabled) {
            Map<String, String> config = new HashMap<>();
            config.put("appId", appId);
            config.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            config.put("nonceStr", UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            config.put("signature", "mock_signature");
            log.info("Mock模式: 返回模拟JS-SDK配置");
            return config;
        }

        try {
            WxJsapiSignature signature = wxMpService.createJsapiSignature(url);
            Map<String, String> config = new HashMap<>();
            config.put("appId", signature.getAppId());
            config.put("timestamp", String.valueOf(signature.getTimestamp()));
            config.put("nonceStr", signature.getNonceStr());
            config.put("signature", signature.getSignature());
            return config;
        } catch (WxErrorException e) {
            log.error("获取JS-SDK签名失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.WECHAT_JSAPI_FAIL);
        }
    }
}