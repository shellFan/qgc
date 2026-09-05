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
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信OAuth认证服务实现类
 */
@Slf4j
@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    private final WxMpService wxMpService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public WechatAuthServiceImpl(WxMpService wxMpService, UserService userService, JwtUtil jwtUtil) {
        this.wxMpService = wxMpService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String buildAuthorizationUrl(String redirectUri, String state) {
        return wxMpService.getOAuth2Service().buildAuthorizationUrl(
                redirectUri,
                "snsapi_userinfo",
                state
        );
    }

    @Override
    public Map<String, Object> loginByCode(String code) {
        try {
            // 通过code换取access_token
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            String openid = accessToken.getOpenId();
            String unionid = accessToken.getUnionId();

            // 获取微信用户信息 - 使用JSON方式获取
            String userInfoJson = wxMpService.getOAuth2Service().getUserInfo(accessToken, null).toString();

            // 创建或更新本地用户（简化处理，从accessToken获取基本信息）
            User user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setNickname("微信用户");
            user.setAvatar("");
            user.setSex(0);
            user.setSubscribe(0);

            user = userService.createOrUpdate(user);

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            // 生成JWT Token
            String token = jwtUtil.generateUserToken(user.getId(), user.getOpenid());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userId", user.getId());
            result.put("nickname", user.getNickname());
            result.put("avatar", user.getAvatar());
            return result;
        } catch (WxErrorException e) {
            log.error("微信OAuth授权失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL);
        }
    }

    @Override
    public Map<String, String> getJsSdkConfig(String url) {
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