-- =============================================
-- RC6 Schema升级脚本
-- Native支付完整接入: 支付类型/二维码/过期时间/请求幂等
-- =============================================

-- 1. 支付订单表新增字段: 支付类型/二维码URL/过期时间/请求ID
ALTER TABLE `qgc_payment_order`
  ADD COLUMN `pay_type` VARCHAR(16) DEFAULT 'JSAPI' COMMENT '支付类型: MOCK/NATIVE/JSAPI' AFTER `trade_type`,
  ADD COLUMN `code_url` VARCHAR(512) DEFAULT NULL COMMENT 'Native支付二维码URL(weixin://wxpay/...)' AFTER `openid`,
  ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT 'Native支付过期时间(5分钟)' AFTER `code_url`,
  ADD COLUMN `request_id` VARCHAR(64) DEFAULT NULL COMMENT '支付请求ID(客户端生成，幂等防重复)' AFTER `expire_time`;

-- 2. 支付请求ID唯一索引: 幂等防重复支付
ALTER TABLE `qgc_payment_order`
  ADD UNIQUE INDEX `uk_request_id` (`request_id`) COMMENT '支付requestId幂等约束';

-- 3. 支付类型索引: Admin按支付类型筛选
ALTER TABLE `qgc_payment_order`
  ADD INDEX `idx_pay_type` (`pay_type`) COMMENT '支付类型索引';

-- 4. Native支付过期索引: 定时任务关闭过期订单
ALTER TABLE `qgc_payment_order`
  ADD INDEX `idx_expire_time` (`expire_time`) COMMENT 'Native支付过期时间索引';

-- 5. 支付回调日志表: 记录微信支付/退款回调原始数据
CREATE TABLE IF NOT EXISTS `qgc_pay_notify_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(16) NOT NULL COMMENT '类型: PAY/REFUND',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信交易号',
  `raw_data` TEXT NOT NULL COMMENT '原始回调数据',
  `process_result` VARCHAR(32) DEFAULT NULL COMMENT '处理结果: SUCCESS/FAIL',
  `error_msg` VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付回调日志表';

-- 6. Schema版本记录
INSERT IGNORE INTO `qgc_schema_version` (`version`, `description`, `applied_at`) VALUES
  ('rc6', 'RC6 Schema升级: Native支付/支付类型/二维码/过期/幂等/回调日志', NOW());