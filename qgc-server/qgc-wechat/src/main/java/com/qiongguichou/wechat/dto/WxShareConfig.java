package com.qiongguichou.wechat.dto;

import lombok.Data;

/**
 * 微信JS-SDK分享配置
 */
@Data
public class WxShareConfig {

    /** 公众号AppId */
    private String appId;

    /** 时间戳 */
    private String timestamp;

    /** 随机字符串 */
    private String nonceStr;

    /** 签名 */
    private String signature;

    /** 分享链接 */
    private String shareUrl;

    /** 分享标题 */
    private String shareTitle;

    /** 分享描述 */
    private String shareDesc;

    /** 分享图标 */
    private String shareImageUrl;
}