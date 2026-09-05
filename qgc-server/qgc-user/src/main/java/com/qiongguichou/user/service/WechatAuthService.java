package com.qiongguichou.user.service;

import java.util.Map;

/**
 * 微信OAuth认证服务接口
 */
public interface WechatAuthService {

    /**
     * 构建微信OAuth2授权URL
     *
     * @param redirectUri 回调地址
     * @param state       状态参数(防CSRF)
     * @return 授权URL
     */
    String buildAuthorizationUrl(String redirectUri, String state);

    /**
     * 通过授权码换取用户信息并登录/注册
     *
     * @param code 授权码
     * @return 包含token和用户信息的Map
     */
    Map<String, Object> loginByCode(String code);

    /**
     * 获取微信JS-SDK签名配置
     *
     * @param url 当前页面URL
     * @return 签名配置
     */
    Map<String, String> getJsSdkConfig(String url);
}