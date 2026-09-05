package com.qiongguichou.wechat.service.impl;

import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.wechat.dto.WxShareConfig;
import com.qiongguichou.wechat.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.common.bean.WxJsapiSignature;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 微信服务实现
 * 支持Mock模式：qgc.wechat.mock-enabled=true时使用Mock实现
 */
@Slf4j
@Service
public class WechatServiceImpl implements WechatService {

    @Value("${qgc.wechat.mock-enabled:true}")
    private boolean mockEnabled;

    @Value("${qgc.wechat.mp.appId:mock_appid}")
    private String appId;

    @Autowired(required = false)
    private WxMpService wxMpService;

    @Override
    public String getOAuthUrl(String redirectUrl, String state) {
        if (mockEnabled) {
            // Mock模式：直接返回一个模拟URL，带code参数
            return redirectUrl + (redirectUrl.contains("?") ? "&" : "?") + "code=mock_code&state=" + state;
        }
        // 真实模式：构造微信OAuth授权URL
        return wxMpService.getOAuth2Service().buildAuthorizationUrl(
                redirectUrl, "snsapi_userinfo", state);
    }

    @Override
    public String handleOAuthCallback(String code) {
        if (mockEnabled) {
            // Mock模式：返回模拟openid
            log.info("Mock模式: 使用模拟openid, code={}", code);
            return "mock_openid_" + System.currentTimeMillis() % 10000;
        }
        try {
            WxOAuth2AccessToken accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            String openid = accessToken.getOpenId();
            log.info("微信OAuth授权成功, openid={}", openid);
            return openid;
        } catch (WxErrorException e) {
            log.error("微信OAuth授权失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL, "微信授权失败: " + e.getMessage());
        }
    }

    @Override
    public WxShareConfig getShareConfig(String url, String title, String desc, String imageUrl) {
        WxShareConfig config = new WxShareConfig();
        config.setShareUrl(url);
        config.setShareTitle(title);
        config.setShareDesc(desc);
        config.setShareImageUrl(imageUrl);

        if (mockEnabled) {
            config.setAppId(appId);
            config.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));
            config.setNonceStr(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            config.setSignature("mock_signature");
            return config;
        }

        try {
            // 使用WxMpService内置方法生成JS-SDK签名
            WxJsapiSignature jsapiSignature = wxMpService.createJsapiSignature(url);
            config.setAppId(jsapiSignature.getAppId());
            config.setTimestamp(String.valueOf(jsapiSignature.getTimestamp()));
            config.setNonceStr(jsapiSignature.getNonceStr());
            config.setSignature(jsapiSignature.getSignature());
        } catch (WxErrorException e) {
            log.error("获取JS-SDK ticket失败: {}", e.getMessage(), e);
            // 降级返回空签名，前端分享功能不可用但不影响页面
            config.setAppId(appId);
            config.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));
            config.setNonceStr(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            config.setSignature("");
        }
        return config;
    }

    @Override
    public String generateQrCode(String scene, String page) {
        if (mockEnabled) {
            return "https://mock.qgc.com/qrcode/" + scene;
        }
        try {
            // 生成小程序码（非公众号二维码）
            // 注意：公众号临时二维码API需要sceneId为int类型
            // 此处使用scene的hash作为sceneId
            int sceneId = Math.abs(scene.hashCode() % 100000);
            me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket ticket =
                    wxMpService.getQrcodeService().qrCodeCreateTmpTicket(sceneId, 2592000);
            return wxMpService.getQrcodeService().qrCodePictureUrl(ticket.getTicket());
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.WECHAT_JSAPI_FAIL, "生成二维码失败");
        }
    }
}