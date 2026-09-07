# QGC 状态枚举规范

> RC5 统一规范：所有状态字段必须使用枚举常量，禁止硬编码状态字符串。

## 枚举清单

### CampaignStatus - 筹款状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| DRAFT | 草稿 | DRAFT |
| PENDING_REVIEW | 待审核 | PENDING_REVIEW |
| ACTIVE | 进行中 | ACTIVE |
| SUCCESS | 已筹满 | SUCCESS |
| EXPIRED | 已到期 | EXPIRED |
| CLOSED | 已关闭 | CLOSED |
| REJECTED | 审核拒绝 | REJECTED |
| RISK_FROZEN | 风控冻结 | RISK_FROZEN |

**状态流转：**
```
DRAFT → PENDING_REVIEW → ACTIVE → SUCCESS
                    ↘ REJECTED → DRAFT(重新编辑)
ACTIVE → EXPIRED
ACTIVE → CLOSED(管理员关闭)
ACTIVE → RISK_FROZEN
```

### PaymentStatus - 支付订单状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| CREATED | 已创建 | CREATED |
| PAYING | 支付中 | PAYING |
| SUCCESS | 支付成功 | SUCCESS |
| CLOSED | 已关闭 | CLOSED |
| REFUNDING | 退款中 | REFUNDING |
| PART_REFUNDED | 部分退款 | PART_REFUNDED |
| REFUNDED | 已退款 | REFUNDED |
| FAIL | 支付失败 | FAIL |

**状态流转：**
```
CREATED → PAYING → SUCCESS → REFUNDING → PART_REFUNDED
                 ↘ CLOSED              ↘ REFUNDED
       ↘ FAIL
```

### SupportStatus - 支持订单状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| CREATED | 已创建 | CREATED |
| PAID | 已支付 | PAID |
| CLOSED | 已关闭 | CLOSED |
| REFUNDING | 退款中 | REFUNDING |
| REFUNDED | 已退款 | REFUNDED |

**状态流转：**
```
CREATED → PAID → REFUNDING → REFUNDED
       ↘ CLOSED
```

### RefundStatus - 退款订单状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| PENDING | 待处理 | PENDING |
| SUCCESS | 退款成功 | SUCCESS |
| FAIL | 退款失败 | FAIL |

### WithdrawStatus - 提现状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| PENDING | 待审核 | PENDING |
| PROCESSING | 处理中 | PROCESSING |
| SUCCESS | 成功 | SUCCESS |
| FAIL | 失败 | FAIL |
| REJECTED | 已拒绝 | REJECTED |

### WithdrawOrderStatus - 提现订单状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| PENDING | 待审核 | PENDING |
| PROCESSING | 处理中 | PROCESSING |
| SUCCESS | 提现成功 | SUCCESS |
| FAIL | 提现失败 | FAIL |
| REJECTED | 审核拒绝 | REJECTED |

> **注意：** WithdrawStatus 和 WithdrawOrderStatus 值域相同，后续版本应合并。

### ReportStatus - 举报状态
| 枚举值 | 含义 | DB存储值 |
|--------|------|----------|
| PENDING | 待处理 | PENDING |
| REVIEWED | 已审核 | REVIEWED |
| RESOLVED | 已处理 | RESOLVED |

### WalletFlowType - 钱包流水类型
| 枚举值 | 含义 |
|--------|------|
| CAMPAIGN_INCOME | 筹款收入 |
| WITHDRAW_APPLY | 提现申请 |
| WITHDRAW_SUCCESS | 提现成功 |
| WITHDRAW_FAIL_RETURN | 提现失败退回 |
| PAYMENT_REFUND | 支付退款 |
| WECHAT_FEE | 微信手续费 |
| ADMIN_ADJUST | 管理员调整 |

## 非枚举状态字段（Integer类型）

以下字段使用 Integer 而非枚举，属于历史遗留，后续版本应枚举化：

| 实体 | 字段 | 值域 |
|------|------|------|
| User | status | 1=正常, 2=封禁 |
| Comment | status | 0=待审核, 1=已通过 |

## 编码规范

### ✅ 正确用法
```java
// 查询条件
.eq(Campaign::getStatus, CampaignStatus.ACTIVE.name())
.in(PaymentOrder::getStatus, PaymentStatus.CREATED.name(), PaymentStatus.PAYING.name())

// 默认参数
@RequestParam(required = false) String status
if (status == null || status.isEmpty()) {
    status = ReportStatus.PENDING.name();
}

// 状态更新
.set(PaymentOrder::getStatus, PaymentStatus.SUCCESS.name())
```

### ❌ 禁止用法
```java
// 禁止硬编码状态字符串
.eq(Campaign::getStatus, "ACTIVE")           // ❌
.eq(WithdrawOrder::getStatus, "PENDING")     // ❌
@RequestParam(defaultValue = "PENDING")      // ❌ (编译期常量无法用枚举)
```

### 微信回调协议值（例外）
微信支付/退款回调返回的 "SUCCESS"/"FAIL" 是微信API协议常量，非项目枚举，保留硬编码：
```java
// ✅ 微信回调协议响应，保留不改
return "SUCCESS";  // 告知微信已收到通知
return "FAIL";     // 告知微信处理失败
"SUCCESS".equals(result.getRefundStatus())  // 微信返回的退款状态
```

## 枚举包路径
```
com.qiongguichou.common.enums
├── CampaignStatus.java
├── PaymentStatus.java
├── SupportStatus.java
├── RefundStatus.java
├── WithdrawStatus.java
├── WithdrawOrderStatus.java
├── ReportStatus.java
└── WalletFlowType.java