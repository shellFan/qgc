package com.qiongguichou.wechat.controller;

import com.qiongguichou.common.result.Result;
import com.qiongguichou.wechat.dto.WxShareConfig;
import com.qiongguichou.wechat.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    @Autowired
    private WechatService wechatService;

    /**
     * 获取微信OAuth授权URL
     */
    @GetMapping("/oauth/url")
    public Result<String> getOAuthUrl(
            @RequestParam String redirectUrl,
            @RequestParam(required = false, defaultValue = "qgc") String state) {
        String oauthUrl = wechatService.getOAuthUrl(redirectUrl, state);
        return Result.success(oauthUrl);
    }

    /**
     * 微信OAuth授权回调
     */
    @GetMapping("/oauth/callback")
    public Result<Map<String, String>> handleOAuthCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state) {
        String openid = wechatService.handleOAuthCallback(code);
        Map<String, String> result = new HashMap<>();
        result.put("openid", openid);
        result.put("state", state);
        return Result.success(result);
    }

    /**
     * 获取JS-SDK分享配置
     */
    @GetMapping("/share/config")
    public Result<WxShareConfig> getShareConfig(
            @RequestParam String url,
            @RequestParam(required = false, defaultValue = "穷鬼筹") String title,
            @RequestParam(required = false, defaultValue = "小额筹款，轻松互助") String desc,
            @RequestParam(required = false) String imageUrl) {
        WxShareConfig shareConfig = wechatService.getShareConfig(url, title, desc, imageUrl);
        return Result.success(shareConfig);
    }

    /**
     * 生成带参数二维码
     */
    @GetMapping("/qrcode")
    public Result<String> generateQrCode(
            @RequestParam String scene,
            @RequestParam(required = false) String page) {
        String qrCodeUrl = wechatService.generateQrCode(scene, page);
        return Result.success(qrCodeUrl);
    }
}