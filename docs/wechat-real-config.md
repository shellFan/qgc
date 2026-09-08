# 微信真实配置清单

## 一、公众号配置（OAuth + JS-SDK + JSAPI支付）

| 配置项 | 环境变量 | 说明 |
|--------|----------|------|
| 公众号AppID | `QGC_WECHAT_MP_APPID` | 微信公众号AppID |
| 公众号AppSecret | `QGC_WECHAT_MP_APPSECRET` | 微信公众号AppSecret |
| OAuth回调域名 | 微信后台配置 | 授权回调域名（不含协议头） |
| JS安全域名 | 微信后台配置 | JS-SDK安全域名 |
| 网页授权范围 | `snsapi_userinfo` | 获取用户昵称头像 |

## 二、微信支付配置（JSAPI）

| 配置项 | 环境变量 | 说明 |
|--------|----------|------|
| 支付AppID | `QGC_PAY_APPID` | 通常与公众号AppID一致 |
| 商户号 | `QGC_PAY_MCH_ID` | 微信支付商户号 |
| APIv2密钥 | `QGC_PAY_MCH_KEY` | 商户平台设置的APIv2密钥（兼容旧接口） |
| APIv3密钥 | `QGC_PAY_API_V3_KEY` | 商户平台设置的APIv3密钥（回调解密） |
| 商户API私钥 | `QGC_PAY_PRIVATE_KEY_PATH` | apiclient_key.pem文件路径 |
| 商户API证书 | `QGC_PAY_CERT_PATH` | apiclient_cert.pem文件路径（退款需要） |
| 证书序列号 | `QGC_PAY_CERTIFICATE_SERIAL_NO` | 商户API证书序列号 |
| 支付回调URL | `QGC_PAY_NOTIFY_URL` | `https://域名/api/payment/notify/pay` |
| 退款回调URL | `QGC_PAY_REFUND_NOTIFY_URL` | `https://域名/api/payment/notify/refund` |

## 三、Staging环境配置模板

```yaml
# application-staging.yml
qgc:
  wechat:
    mock-enabled: false  # 关闭Mock，使用真实微信
    mp:
      app-id: ${QGC_WECHAT_MP_APPID}
      app-secret: ${QGC_WECHAT_MP_APPSECRET}
  pay:
    mock-enabled: false  # 关闭Mock，使用真实支付
    app-id: ${QGC_PAY_APPID}
    mch-id: ${QGC_PAY_MCH_ID}
    mch-key: ${QGC_PAY_MCH_KEY}
    api-v3-key: ${QGC_PAY_API_V3_KEY}
    private-key-path: ${QGC_PAY_PRIVATE_KEY_PATH}
    certificate-serial-no: ${QGC_PAY_CERTIFICATE_SERIAL_NO}
    cert-path: ${QGC_PAY_CERT_PATH}
    notify-url: ${QGC_PAY_NOTIFY_URL}
    refund-notify-url: ${QGC_PAY_REFUND_NOTIFY_URL}
```

## 四、微信后台配置检查清单

- [ ] 公众号已认证（服务号）
- [ ] 已开通微信支付
- [ ] 已绑定商户号
- [ ] OAuth2.0网页授权域名已配置
- [ ] JS接口安全域名已配置
- [ ] 支付授权目录已配置（如 `https://staging.qgc.example.com/`）
- [ ] APIv3密钥已设置
- [ ] API证书已下载并部署
- [ ] 回调URL可达（需HTTPS）

## 五、DEV环境（Mock模式）

```yaml
# application-dev.yml
qgc:
  wechat:
    mock-enabled: true   # Mock模式，不依赖真实微信
  pay:
    mock-enabled: true   # Mock模式，支付直接成功