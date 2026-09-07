-- =============================================
-- RC4 Schema升级脚本
-- 终极验收修复: 幂等约束/类型升级/新表/配置化
-- =============================================

-- 1. 钱包流水业务唯一索引: 防止同一业务重复记账
-- 幂等化: 同一用户+同一类型+同一关联ID只能有一条流水
ALTER TABLE `qgc_wallet_flow`
  ADD UNIQUE INDEX `uk_biz` (`user_id`, `type`, `related_id`) COMMENT '业务唯一约束防重复记账';

-- 2. 广告统计表字段类型升级: INT → BIGINT 防溢出
ALTER TABLE `qgc_ad_stat`
  MODIFY COLUMN `impression_count` BIGINT DEFAULT 0 COMMENT '展示次数',
  MODIFY COLUMN `click_count` BIGINT DEFAULT 0 COMMENT '点击次数';

-- 3. 分享转化记录表: DB层幂等防重复计数
-- 同一支持订单对同一分享码只计一次转化
CREATE TABLE IF NOT EXISTS `qgc_share_conversion` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `share_code` VARCHAR(64) NOT NULL COMMENT '分享码',
  `visitor_key` VARCHAR(128) NOT NULL COMMENT '访客标识(SHA-256 hash后)',
  `support_no` VARCHAR(64) DEFAULT NULL COMMENT '支持订单号(投喂转化时记录)',
  `campaign_id` BIGINT DEFAULT NULL COMMENT '筹款ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '分享者用户ID',
  `conversion_type` VARCHAR(32) NOT NULL COMMENT '转化类型: VISIT/SUPPORT',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_visit` (`share_code`, `visitor_key`) COMMENT '访客去重唯一约束',
  UNIQUE KEY `uk_support` (`share_code`, `support_no`) COMMENT '支持去重唯一约束',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享转化记录表(DB幂等)';

-- 4. 提现手续费配置: 从system_config读取, 替代硬编码
-- 初始化默认配置(如已有则INSERT IGNORE跳过)
INSERT IGNORE INTO `qgc_system_config` (`config_key`, `config_value`, `remark`, `create_time`, `update_time`)
VALUES
  ('withdraw.fee_rate', '0.006', '提现手续费费率(0.6%)', NOW(), NOW()),
  ('withdraw.min_fee', '100', '最低手续费(分)=1元', NOW(), NOW()),
  ('withdraw.min_amount', '100', '最低提现金额(分)=1元', NOW(), NOW()),
  ('withdraw.max_amount', '200000', '最高提现金额(分)=2000元', NOW(), NOW());

-- 5. 支付订单条件更新索引: 优化WHERE status IN ('CREATED','PAYING')查询
ALTER TABLE `qgc_payment_order`
  ADD INDEX `idx_status_create` (`status`, `create_time`) COMMENT '支付回调条件更新索引';

-- 6. 支持订单状态索引: 优化状态查询
ALTER TABLE `qgc_support_order`
  ADD INDEX `idx_status_create` (`status`, `create_time`) COMMENT '支持订单状态+创建时间索引';

-- 7. 限流配置初始化
INSERT IGNORE INTO `qgc_system_config` (`config_key`, `config_value`, `remark`, `create_time`, `update_time`)
VALUES
  ('ratelimit.create_campaign', '5/3600', '创建筹款限流: 每小时5次', NOW(), NOW()),
  ('ratelimit.support', '10/60', '投喂限流: 每分钟10次', NOW(), NOW()),
  ('ratelimit.comment', '10/60', '评论限流: 每分钟10次', NOW(), NOW()),
  ('ratelimit.like', '30/60', '点赞限流: 每分钟30次', NOW(), NOW()),
  ('ratelimit.share', '20/60', '分享限流: 每分钟20次', NOW(), NOW()),
  ('ratelimit.withdraw', '3/3600', '提现限流: 每小时3次', NOW(), NOW());

-- 8. 应用版本配置
INSERT IGNORE INTO `qgc_system_config` (`config_key`, `config_value`, `remark`, `create_time`, `update_time`)
VALUES
  ('qgc.version', 'rc4', '应用版本号', NOW(), NOW());