-- =============================================
-- RC7 Schema升级脚本
-- 产品闭环: 分享模板初始化/审核开关/用户协议/内容合规
-- =============================================
USE `qiongguichou`;

-- 1. 分享模板初始化数据
INSERT IGNORE INTO `qgc_share_template` (`id`, `category_id`, `title_template`, `desc_template`, `image_url`, `sort`, `enabled`) VALUES
(1, NULL, '帮帮这个穷鬼！{title}', '目标{target}元，已筹{raised}元，还差{remaining}元', NULL, 1, 1),
(2, 1, 'V我50，疯狂星期四！{title}', '你的一块钱，可能就是我鸡翅上的一层脆皮。', NULL, 2, 1),
(3, 2, '来份薯条续续命！{title}', '一根薯条也是爱，一份大薯更是情。', NULL, 3, 1),
(4, 3, '今天不喝奶茶会死！{title}', '你的投喂，就是我的续命水。', NULL, 4, 1),
(5, 9, '随便V我点，我不挑！{title}', '多少都是爱，穷鬼不挑。', NULL, 5, 1);

-- 2. 用户协议与隐私政策配置
INSERT IGNORE INTO `qgc_system_config` (`config_key`, `config_value`, `description`) VALUES
('user_agreement.version', '1.0', '用户协议版本号'),
('user_agreement.url', 'https://pay.21zuo.com/h5/agreement.html', '用户协议URL'),
('privacy_policy.version', '1.0', '隐私政策版本号'),
('privacy_policy.url', 'https://pay.21zuo.com/h5/privacy.html', '隐私政策URL'),
('content_compliance.enabled', 'true', '是否启用内容合规提示'),
('content_compliance.disclaimer', '本平台仅为信息展示及资金流转中介，不对筹款项目真实性负责', '内容合规免责声明'),
('share.poster.enabled', 'true', '是否启用分享海报功能'),
('share.qrcode.enabled', 'true', '是否启用二维码功能'),
('h5.tabbar.items', 'home,square,create,ranking,my', 'H5底部TabBar项目(逗号分隔)');

-- 3. qgc_campaign表增加审核相关字段
ALTER TABLE `qgc_campaign`
  ADD COLUMN IF NOT EXISTS `reviewed_at` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `reject_reason`,
  ADD COLUMN IF NOT EXISTS `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID' AFTER `reviewed_at`;

-- 4. qgc_ad表增加positionCode字段(冗余,方便查询)
ALTER TABLE `qgc_ad`
  ADD COLUMN IF NOT EXISTS `position_code` VARCHAR(64) DEFAULT NULL COMMENT '广告位编码(冗余)' AFTER `position_id`,
  ADD COLUMN IF NOT EXISTS `view_count` INT DEFAULT 0 COMMENT '展示次数' AFTER `link_url`,
  ADD COLUMN IF NOT EXISTS `click_count` INT DEFAULT 0 COMMENT '点击次数' AFTER `view_count`;

-- 5. qgc_user表增加用户协议同意标记
ALTER TABLE `qgc_user`
  ADD COLUMN IF NOT EXISTS `agreement_version` VARCHAR(16) DEFAULT NULL COMMENT '同意的用户协议版本' AFTER `subscribe`,
  ADD COLUMN IF NOT EXISTS `agreement_time` DATETIME DEFAULT NULL COMMENT '同意协议时间' AFTER `agreement_version`;

-- 6. 审核日志表(管理员审核操作记录)
CREATE TABLE IF NOT EXISTS `qgc_review_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `admin_username` VARCHAR(64) DEFAULT NULL COMMENT '管理员用户名',
  `target_type` VARCHAR(32) NOT NULL COMMENT '目标类型: CAMPAIGN/COMMENT/REPORT/WITHDRAW',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `action` VARCHAR(32) NOT NULL COMMENT '操作: APPROVE/REJECT/FREEZE/CLOSE/HANDLE',
  `reason` VARCHAR(512) DEFAULT NULL COMMENT '原因/备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核日志表';

-- 7. Schema版本记录
INSERT IGNORE INTO `qgc_schema_version` (`version`, `description`, `applied_at`) VALUES
  ('rc7', 'RC7 Schema升级: 分享模板/审核日志/用户协议/内容合规', NOW());

-- 8. 更新版本配置
UPDATE `qgc_system_config` SET `config_value` = 'rc7', `update_time` = NOW()
  WHERE `config_key` = 'qgc.version';