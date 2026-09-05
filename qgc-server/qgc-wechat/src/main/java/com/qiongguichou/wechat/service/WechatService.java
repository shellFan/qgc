package com.qiongguichou.wechat.service;

import com.qiongguichou.wechat.dto.WxShareConfig;

/**
 * 微信服务接口
 */
public interface WechatService {

    /**
     * 获取OAuth授权URL
     */
    String getOAuthUrl(String redirectUrl, String state);

    /**
     * OAuth回调换取openid
     */
    String handleOAuthCallback(String code);

    /**
     * 获取JS-SDK分享配置
     */
    WxShareConfig getShareConfig(String url, String title, String desc, String imageUrl);

    /**
     * 生成带参数的小程序码/公众号二维码
     */
    String generateQrCode(String scene, String page);
}