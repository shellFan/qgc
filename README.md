# 穷鬼筹 V1.0

一个有趣的微信H5筹款应用 - "穷鬼互助，人间有爱"

## 项目结构

```
qgc/
├── qgc-server/              # Java后端 (Spring Boot 3 + MyBatis-Plus)
│   ├── qgc-common/           # 公共模块(错误码、工具类、枚举)
│   ├── qgc-core/             # 核心模块(全局配置、拦截器)
│   ├── qgc-user/             # 用户模块(微信登录、Dev Mock登录)
│   ├── qgc-campaign/         # 筹款模块(创建、详情、返图、分类)
│   ├── qgc-payment/          # 支付模块(微信支付、Mock支付、退款)
│   ├── qgc-wallet/           # 钱包模块(余额、提现、流水)
│   ├── qgc-content/          # 内容模块(评论、通知、举报、敏感词)
│   ├── qgc-admin/            # 管理后台模块(审核、提现审批、仪表盘)
│   ├── qgc-wechat/           # 微信模块(JS-SDK、二维码、分享)
│   ├── qgc-job/              # 定时任务模块(过期筹款关闭)
│   └── qgc-server-main/      # 启动模块
├── qgc-h5/                   # H5前端 (Vue3 + Vite + Vant4 + Pinia)
├── qgc-admin-web/            # 管理后台前端 (Vue3 + Vite + ElementPlus + Pinia)
├── sql/                      # 数据库脚本
│   ├── 01_schema.sql         # 建表脚本(29张表)
│   ├── 02_init_data.sql      # 初始化数据(分类、广告位、骚话、配置)
│   └── 03_admin.sql          # 管理员初始化(角色、admin用户)
└── pom.xml                   # Maven父POM
```

## 技术栈

### 后端
- Java 17 + Spring Boot 3.2
- MyBatis-Plus 3.5
- Redisson (分布式锁)
- MySQL 5.6+ / 8.0
- Redis
- 微信支付 SDK

### 前端
- Vue 3.4 + Vite 5
- Vant 4 (H5移动端UI)
- Element Plus 2.7 (管理后台UI)
- Pinia (状态管理)
- Axios (HTTP请求)

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 5.6+ / 8.0
- Redis 6+

### 1. 数据库初始化

```sql
-- 按顺序执行SQL脚本
source sql/01_schema.sql;
source sql/02_init_data.sql;
source sql/03_admin.sql;
```

### 2. 后端启动

```bash
cd qgc-server
mvn clean package -DskipTests
java -jar qgc-server-main/target/qgc-server-main.jar
```

开发环境默认配置(Mock模式):
- Mock微信登录: 开启
- Mock微信支付: 开启
- 端口: 8080

### 3. H5前端

```bash
cd qgc-h5
npm install
npm run dev     # 开发模式 http://localhost:3000
npm run build   # 生产构建
```

### 4. 管理后台

```bash
cd qgc-admin-web
npm install
npm run dev     # 开发模式 http://localhost:3001
npm run build   # 生产构建
```

默认管理员账号: admin / Admin@123456

## 核心业务流程

1. **用户登录**: 微信授权登录(DEV模式支持Mock登录)
2. **发起筹款**: 选择分类 → 填写标题/描述/金额 → 发起
3. **投喂支持**: 浏览广场 → 查看详情 → 选择金额 → 支付
4. **超额退款**: 筹款达标后超额部分自动退款
5. **返图**: 发起人上传返图证明
6. **提现**: 发起人申请提现 → 管理员审核 → 打款

## 安全特性

- 金额统一Long/BIGINT/分，前端BigDecimal"元"→MoneyUtil.yuanToFen转换
- 支付回调金额校验(paidAmount必须等于orderAmount)
- 本人禁止投喂自己的筹款
- 钱包流水balanceAfter一致性: 乐观锁更新后重新查询
- 退款SQL条件: 负数addAmount不受target_amount约束
- 生产环境Mock保护: prod profile下mock-enabled=true则System.exit(1)
- 支付回调幂等: transaction_id唯一索引+订单状态判断

## API概览

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证 | /api/auth, /dev | 微信登录、Dev Mock登录 |
| 用户 | /api/user | 用户信息、我的筹款、我的支持 |
| 筹款 | /api/campaign | 创建、详情、列表、关闭、分类、随机骚话 |
| 返图 | /api/campaign/proof | 创建返图、点赞 |
| 支付 | /api/payment | 投喂支付、回调 |
| 钱包 | /api/wallet | 余额、提现、流水 |
| 评论 | /api/comment | 评论CRUD |
| 通知 | /api/notification | 消息列表、已读 |
| 管理后台 | /admin/api | 仪表盘、筹款审核、用户管理、提现审批 |