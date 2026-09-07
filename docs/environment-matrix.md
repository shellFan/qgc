# QGC 环境矩阵

> RC5 规范：三环境配置隔离，环境变量驱动，禁止硬编码敏感信息。

## 环境概览

| 配置项 | Dev (默认) | Staging | Production |
|--------|-----------|---------|------------|
| Profile | (无) | staging | prod |
| Spring Profile | - | `application-staging.yml` | `application-prod.yml` |
| 启动命令 | `mvn spring-boot:run` | `java -jar --spring.profiles.active=staging` | `java -jar --spring.profiles.active=prod` |
| Mock登录 | ✅ true | ❌ false | ❌ false |
| Mock微信 | ✅ true | ❌ false | ❌ false |
| Mock支付 | ✅ true | ❌ false | ❌ false |
| Swagger | ✅ 开启 | ✅ 开启(内网) | ❌ 关闭 |
| SQL日志 | StdOutImpl | StdOutImpl | NoLoggingImpl |
| 日志级别 | debug | debug | info |
| Redis DB | 0 | 1 | 0(独立实例) |
| CORS | localhost | 具体域名 | 具体域名 |

## 必须设置的环境变量

### Staging环境
| 变量名 | 说明 | 示例 |
|--------|------|------|
| SPRING_DATASOURCE_URL | 数据库连接 | jdbc:mysql://staging-db:3306/qiongguichou |
| SPRING_DATASOURCE_USERNAME | 数据库用户 | qgc_staging |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | (强密码) |
| SPRING_REDIS_HOST | Redis地址 | staging-redis |
| JWT_SECRET | JWT密钥 | (随机64+字符) |
| CORS_ALLOWED_ORIGINS | 允许的来源 | https://staging.qgc.example.com |
| WX_MP_APPID | 微信测试公众号AppID | wx1234567890 |
| WX_MP_SECRET | 微信测试公众号Secret | (微信后台获取) |
| WX_MP_TOKEN | 微信公众号Token | (自定义) |
| WX_PAY_APPID | 微信支付AppID | wx1234567890 |
| WX_PAY_MCH_ID | 微信支付商户号 | 1234567890 |
| WX_PAY_MCH_KEY | 微信支付商户密钥 | (微信后台获取) |
| WX_PAY_NOTIFY_URL | 支付回调URL | https://staging-api.qgc.example.com/api/pay/notify |

### Production环境
在Staging基础上额外要求：
| 变量名 | 说明 |
|--------|------|
| SPRING_REDIS_PASSWORD | Redis密码(必须) |
| WX_MP_AES_KEY | 消息加密密钥(必须) |
| CORS_ALLOWED_ORIGINS | 仅限生产域名 |

## Docker部署

### Staging
```bash
docker run -d --name qgc-staging \
  -p 8080:8080 \
  --spring.profiles.active=staging \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://staging-db:3306/qiongguichou \
  -e SPRING_DATASOURCE_USERNAME=qgc_staging \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e SPRING_REDIS_HOST=staging-redis \
  -e JWT_SECRET=<secret> \
  -e CORS_ALLOWED_ORIGINS=https://staging.qgc.example.com \
  -e WX_MP_APPID=<appid> \
  -e WX_MP_SECRET=<secret> \
  -e WX_MP_TOKEN=<token> \
  -e WX_PAY_APPID=<appid> \
  -e WX_PAY_MCH_ID=<mchid> \
  -e WX_PAY_MCH_KEY=<mchkey> \
  -e WX_PAY_NOTIFY_URL=https://staging-api.qgc.example.com/api/pay/notify \
  qgc-server:latest
```

### Production
```bash
docker run -d --name qgc-prod \
  -p 8080:8080 \
  --spring.profiles.active=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/qiongguichou \
  -e SPRING_DATASOURCE_USERNAME=qgc_prod \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e SPRING_REDIS_HOST=prod-redis \
  -e SPRING_REDIS_PASSWORD=<password> \
  -e JWT_SECRET=<secret> \
  -e CORS_ALLOWED_ORIGINS=https://qgc.example.com \
  -e WX_MP_APPID=<appid> \
  -e WX_MP_SECRET=<secret> \
  -e WX_MP_TOKEN=<token> \
  -e WX_MP_AES_KEY=<aeskey> \
  -e WX_PAY_APPID=<appid> \
  -e WX_PAY_MCH_ID=<mchid> \
  -e WX_PAY_MCH_KEY=<mchkey> \
  -e WX_PAY_NOTIFY_URL=https://api.qgc.example.com/api/pay/notify \
  qgc-server:latest
```

## Production Guard

生产环境启动时自动检查以下条件，不满足则拒绝启动：

1. `qgc.auth.mock-enabled` 必须为 `false`
2. `qgc.wechat.mock-enabled` 必须为 `false`
3. `qgc.pay.mock-enabled` 必须为 `false`
4. `qgc.jwt.secret` 不能是默认值 `qiongguichou2026secretkey_dev_only`
5. `qgc.jwt.secret` 不能为空
6. `cors.allowed-origins` 不能为空
7. `cors.allowed-origins` 不能包含 `*`

## 配置文件对应

| 文件 | 用途 |
|------|------|
| `application.yml` | 默认配置(Dev环境) |
| `application-staging.yml` | Staging覆盖配置 |
| `application-prod.yml` | Production覆盖配置 |
| `deploy/.env.example` | 环境变量模板 |
| `deploy/docker-compose.yml` | Docker Compose编排 |