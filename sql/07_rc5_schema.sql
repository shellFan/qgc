-- =============================================
-- RC5 Schema升级脚本
-- Staging上线演练: Schema版本表/提现防护/支付审计/对账
-- =============================================

-- 1. Schema版本跟踪表: 记录每个Schema迁移版本
CREATE TABLE IF NOT EXISTS `qgc_schema_version` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `version` VARCHAR(32) NOT NULL COMMENT '版本号(如rc4, rc5)',
  `description` VARCHAR(256) NOT NULL COMMENT '版本描述',
  `applied_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '应用时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Schema版本跟踪表';

-- 初始化已有版本记录
INSERT IGNORE INTO `qgc_schema_version` (`version`, `description`, `applied_at`) VALUES
  ('rc1', '初始Schema', '2026-01-01 00:00:00'),
  ('rc2', 'RC2 Schema升级', '2026-02-01 00:00:00'),
  ('rc3', 'RC3 Schema升级', '2026-03-01 00:00:00'),
  ('rc4', 'RC4 Schema升级: 幂等约束/类型升级/分享转化表', '2026-04-01 00:00:00'),
  ('rc5', 'RC5 Schema升级: 版本表/提现防护/支付审计/对账', NOW());

-- 2. 提现订单审核备注字段(新增)
ALTER TABLE `qgc_withdraw_order`
  ADD COLUMN `admin_remark` VARCHAR(512) DEFAULT NULL COMMENT '管理员审核备注' AFTER `reject_reason`;

-- 3. 支持订单支付requestId唯一索引: 幂等防重复支付
ALTER TABLE `qgc_support_order`
  ADD UNIQUE INDEX `uk_request_id` (`request_id`) COMMENT '支付requestId幂等约束';

-- 4. 筹款分享码索引: Share归因安全校验
ALTER TABLE `qgc_campaign`
  ADD INDEX `idx_share_code` (`share_code`) COMMENT '分享码索引(归因校验)';

-- 5. 举报处理时间字段
ALTER TABLE `qgc_report`
  ADD COLUMN `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间' AFTER `handle_remark`;

-- 6. 用户封禁检查索引: JWT有效但用户已封禁时快速查询
ALTER TABLE `qgc_user`
  ADD INDEX `idx_status` (`status`) COMMENT '用户状态索引(封禁检查)';

-- 7. 版本配置更新
UPDATE `qgc_system_config` SET `config_value` = 'rc5', `update_time` = NOW()
  WHERE `config_key` = 'qgc.version';