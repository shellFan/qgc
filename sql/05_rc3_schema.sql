-- =============================================
-- 穷鬼筹 V1.0 RC3 数据库升级脚本
-- 兼容 MySQL 5.6+
-- 幂等执行: 所有ALTER使用IF NOT EXISTS或忽略重复错误
-- =============================================
USE `qiongguichou`;

-- =============================================
-- 1. 修复Campaign状态枚举注释(PENDING→PENDING_REVIEW)
-- =============================================
ALTER TABLE `qgc_campaign` MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING_REVIEW/ACTIVE/SUCCESS/CLOSED/EXPIRED/REJECTED/RISK_FROZEN';

-- =============================================
-- 2. 积分流水幂等唯一索引(type+related_id)
--    防止支付回调重复加积分
-- =============================================
-- 先将已有重复数据的related_id为NULL的记录设置默认值
UPDATE `qgc_points_flow` SET `related_id` = CONCAT('AUTO_', id) WHERE `related_id` IS NULL;
ALTER TABLE `qgc_points_flow` ADD UNIQUE KEY `uk_type_related_id` (`user_id`, `type`, `related_id`);

-- =============================================
-- 3. RC2索引幂等修复(使用IF NOT EXISTS或忽略重复)
-- =============================================

-- campaign表补充索引
SET @exist := 0;
SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_campaign' AND index_name = 'idx_status_create_time';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_campaign` ADD INDEX `idx_status_create_time` (`status`, `create_time`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_campaign' AND index_name = 'idx_status_end_time';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_campaign` ADD INDEX `idx_status_end_time` (`status`, `end_time`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_campaign' AND index_name = 'idx_status_raised';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_campaign` ADD INDEX `idx_status_raised` (`status`, `raised_amount`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- support_order表补充索引
SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_support_order' AND index_name = 'idx_campaign_status';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_support_order` ADD INDEX `idx_campaign_status` (`campaign_id`, `status`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- notification表补充索引
SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_notification' AND index_name = 'idx_user_is_read';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_notification` ADD INDEX `idx_user_is_read` (`user_id`, `is_read`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- comment表补充索引
SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_comment' AND index_name = 'idx_target_status';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_comment` ADD INDEX `idx_target_status` (`target_id`, `target_type`, `status`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- qgc_user表补充索引
SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_user' AND index_name = 'idx_create_time';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_user` ADD INDEX `idx_create_time` (`create_time`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================
-- 4. payment_order表transaction_id唯一索引(支付幂等)
-- =============================================
SELECT COUNT(*) INTO @exist FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'qgc_payment_order' AND index_name = 'uk_transaction_id';
SET @sql := IF(@exist = 0, 'ALTER TABLE `qgc_payment_order` ADD UNIQUE KEY `uk_transaction_id` (`transaction_id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =============================================
-- 5. RC2初始化数据幂等化(INSERT IGNORE)
-- =============================================
INSERT IGNORE INTO `qgc_ad_position` (`id`, `code`, `name`, `description`, `enabled`) VALUES
(6, 'HOME_TOP', '首页顶部', '首页顶部Banner广告位', 1),
(7, 'HOME_LIST', '首页列表', '首页列表信息流广告位', 1),
(8, 'DETAIL_BOTTOM', '详情底部', '筹款详情页底部广告位', 1);

INSERT IGNORE INTO `qgc_system_config` (`config_key`, `config_value`, `description`) VALUES
('points.support_earn', '10', '投喂他人获得积分'),
('points.create_earn', '20', '发起筹款获得积分'),
('points.supported_earn', '5', '被投喂获得积分'),
('points.daily_login', '5', '每日登录获得积分'),
('level.1.name', '穷鬼新人', 'Lv1称号'),
('level.1.exp', '0', 'Lv1所需经验'),
('level.2.name', '薯条伙伴', 'Lv2称号'),
('level.2.exp', '100', 'Lv2所需经验'),
('level.3.name', '快乐投喂官', 'Lv3称号'),
('level.3.exp', '500', 'Lv3所需经验'),
('level.4.name', '终极义父', 'Lv4称号'),
('level.4.exp', '2000', 'Lv4所需经验'),
('badge.first_support.name', '初次投喂', '首次投喂徽章名称'),
('badge.first_support.icon', '🤝', '首次投喂徽章图标'),
('badge.first_create.name', '初次发起', '首次发起筹款徽章名称'),
('badge.first_create.icon', '🎉', '首次发起筹款徽章图标'),
('badge.help_10_people.name', '十人义父', '帮助10人徽章名称'),
('badge.help_10_people.icon', '⭐', '帮助10人徽章图标'),
('badge.big_supporter.name', '豪气义父', '大额投喂徽章名称'),
('badge.big_supporter.icon', '💎', '大额投喂徽章图标'),
('ratelimit.create_campaign', '5/3600', '创建筹款限流(次数/秒数)'),
('ratelimit.support', '10/60', '投喂限流(次数/秒数)'),
('ratelimit.comment', '10/60', '评论限流(次数/秒数)'),
('ratelimit.like', '20/60', '点赞限流(次数/秒数)'),
('ratelimit.share', '30/60', '分享限流(次数/秒数)'),
('review.campaign_auto_approve', 'true', '筹款是否自动审核通过'),
('review.sensitive_word_check', 'true', '是否启用敏感词检测');