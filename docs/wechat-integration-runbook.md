# 微信JSAPI支付集成手册

## 一、架构概览

### 服务职责分离

| 服务 | 模块 | 职责 |
|------|------|------|
| WechatAuthService | qgc-user | OAuth授权、获取openid、JS-SDK签名 |
| WechatServiceImpl | qgc-wechat | 公众号能力（OAuth/分享/二维码） |
| PaymentServiceImpl | qgc-payment | JSAPI下单、支付回调、退款 |
| WalletService | qgc-wallet | 钱包余额、提现（与退款分离） |

### Mock模式

所有微信相关服务均支持Mock模式，通过配置开关控制：
- `qgc.wechat.mock-enabled=true`：OAuth/JS-SDK使用模拟数据
- `qgc.pay.mock-enabled=true`：支付直接成功，不调用微信

DEV环境默认Mock开启，STAGING/PROD环境关闭。

## 二、OAuth授权流程

```
用户访问H5 → 检测未登录 → 重定向微信OAuth
→ 用户授权 → 回调带code → 后端换openid+token
→ 前端存储token → 后续请求携带token
```

### state参数（防CSRF + 保留上下文）

state格式：`{随机字符串}_{shareCode}`
- 随机字符串：防CSRF
- shareCode：保留分享归因上下文

### 关键端点

- `GET /api/auth/authorize-url`：获取OAuth授权URL
- `GET /api/auth/callback?code=xxx&state=xxx`：OAuth回调
- `GET /api/auth/jssdk-config?url=xxx`：获取JS-SDK签名配置

## 三、JSAPI支付流程

### 时序图

```
前端(Pay.vue)          后端(PaymentController)       微信支付
    |                        |                          |
    |--POST /pay------------>|                          |
    |                        |--createOrderV3---------->|
    |                        |<--prepay_id-------------|
    |<--wxPayParams---------|                          |
    |                        |                          |
    |--WeixinJSBridge.invoke|                          |
    |  (getBrandWCPayRequest)|                          |
    |                        |                          |
    |  用户完成支付           |                          |
    |                        |<--notify-----------------|
    |                        |  handlePayNotify()       |
    |                        |  金额校验+幂等+入账      |
    |                        |                          |
    |--GET /{orderNo}/status|                          |
    |<--status=SUCCESS------|                          |
    |  显示成功页面           |                          |
```

### 关键规则

1. **禁止相信客户端**：`chooseWXPay success`只代表前端调用结果，必须后端notify/query确认
2. **前端轮询**：支付后每1秒轮询`GET /api/payment/{orderNo}/status`，最多30次
3. **用户取消**：不标FAIL，订单保持CREATED/PAYING，由timeout job关闭
4. **金额严格校验**：微信回调金额必须==payment.expectedPaidAmount，不一致拒绝入账
5. **幂等处理**：transaction_id唯一索引 + 订单状态判断

## 四、前端JSAPI集成要点

### Pay.vue关键逻辑

```javascript
// 1. 检测微信浏览器
function isWechatBrowser() {
  return /micromessenger/i.test(navigator.userAgent)
}

// 2. 调用微信支付（WeixinJSBridge方式）
WeixinJSBridge.invoke('getBrandWCPayRequest', {
  appId, timeStamp, nonceStr,
  package: packageValue,  // 注意：字段名是package，值是"prepay_id=xxx"
  signType: 'RSA',
  paySign
}, callback)

// 3. 回调处理
// ok → 开始轮询后端状态
// cancel → 提示"已取消支付"，不标FAIL
// fail → 提示错误信息

// 4. 轮询确认
// GET /api/payment/{orderNo}/status
// SUCCESS → 显示成功
// FAIL/CLOSED → 显示失败
// CREATED/PAYING → 继续轮询
```

### Mock模式前端处理

后端返回`mock: true`时，前端直接显示成功弹窗，不调用微信支付。

## 五、后端支付核心逻辑

### PaymentServiceImpl.pay()

1. 分布式锁（防超额）
2. requestId幂等（防重复提交）
3. shareCode归因验证
4. 创建SupportOrder + PaymentOrder
5. Mock模式：handleMockPaySuccess直接成功
6. 真实模式：createWxOrder→createOrderV3(JSAPI)

### PaymentServiceImpl.handlePayNotify()

1. Mock模式直接返回SUCCESS
2. APIv3回调验签+解密
3. transaction_id幂等检查
4. 金额校验（微信回调金额==订单金额）
5. DB条件更新（status=CREATED→SUCCESS）
6. processPaySuccess：事务内support/campaign/wallet
7. AFTER_COMMIT：发布事件（通知/积分/徽章/分享统计/缓存）

## 六、0.01元支付测试流程

1. 确保STAGING环境`mock-enabled=false`
2. 在微信内打开H5页面
3. OAuth授权获取openid
4. 选择0.01元投喂
5. 确认JSAPI支付弹窗
6. 完成支付
7. 验证：后端日志显示notify收到+金额校验通过+订单状态SUCCESS
8. 验证：前端轮询显示成功

## 七、退款 vs 提现

| 维度 | 退款(Refund) | 提现(Withdraw) |
|------|-------------|----------------|
| 触发 | 支持者/系统 | 发起人 |
| 方向 | 钱回支持者 | 钱给发起人 |
| 服务 | PaymentService | WalletService |
| 微信API | 退款API | 企业付款到零钱 |
| 回调 | /notify/refund | 无（同步结果） |

## 八、常见问题排查

### Q: 支付后前端一直轮询不到SUCCESS
A: 检查notify URL是否可达、HTTPS证书是否正确、APIv3密钥是否匹配

### Q: WeixinJSBridge.invoke报错
A: 确认JS-SDK配置正确、url签名与当前页面URL一致、支付授权目录已配置

### Q: 回调验签失败
A: 检查APIv3密钥、证书序列号、私钥文件路径

### Q: Mock模式下前端不显示成功
A: 检查PayResult.mock字段是否为true、前端是否正确判断mock分支