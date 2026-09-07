-- =============================================
-- 穷鬼筹 V1.0 RC2 数据库升级脚本
-- 兼容 MySQL 5.6+
-- =============================================
USE `qiongguichou`;

-- =============================================
-- 用户等级系统
-- =============================================

-- 用户等级表
DROP TABLE IF EXISTS `qgc_user_level`;
CREATE TABLE `qgc_user_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `level` INT NOT NULL DEFAULT 1 COMMENT '等级: 1穷鬼新人/2薯条伙伴/3快乐投喂官/4终极义父',
  `exp` INT DEFAULT 0 COMMENT '经验值',
  `title` VARCHAR(32) DEFAULT '穷鬼新人' COMMENT '等级称号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户等级表';

-- 用户徽章表
DROP TABLE IF EXISTS `qgc_user_badge`;
CREATE TABLE `qgc_user_badge` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `badge_code` VARCHAR(32) NOT NULL COMMENT '徽章编码: first_support/first_create/help_10_people/big_supporter',
  `badge_name` VARCHAR(64) NOT NULL COMMENT '徽章名称',
  `badge_icon` VARCHAR(32) DEFAULT NULL COMMENT '徽章图标',
  `earned_time` DATETIME NOT NULL COMMENT '获得时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_code`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户徽章表';

-- 用户积分表
DROP TABLE IF EXISTS `qgc_user_points`;
CREATE TABLE `qgc_user_points` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `points` INT DEFAULT 0 COMMENT '当前积分',
  `total_earned` INT DEFAULT 0 COMMENT '累计获得积分',
  `total_spent` INT DEFAULT 0 COMMENT '累计消费积分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分表';

-- 积分流水表
DROP TABLE IF EXISTS `qgc_points_flow`;
CREATE TABLE `qgc_points_flow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '类型: SUPPORT_EARN/CREATE_EARN/BADGE_EARN/DAILY_LOGIN/DECORATION_SPEND',
  `amount` INT NOT NULL COMMENT '变动积分(正为获得,负为消费)',
  `balance_after` INT NOT NULL COMMENT '变动后积分',
  `related_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- =============================================
-- 分享裂变系统
-- =============================================

-- 分享记录表
DROP TABLE IF EXISTS `qgc_share_record`;
CREATE TABLE `qgc_share_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '分享者用户ID',
  `campaign_id` BIGINT DEFAULT NULL COMMENT '筹款ID',
  `share_type` VARCHAR(32) NOT NULL COMMENT '分享类型: WECHAT_FRIEND/WECHAT_GROUP/TIMELINE/QRCODE/COPY_LINK',
  `source` VARCHAR(32) DEFAULT NULL COMMENT '来源渠道: wechat_friend/wechat_group/timeline',
  `share_code` VARCHAR(64) DEFAULT NULL COMMENT '分享码(用于追踪)',
  `visitor_count` INT DEFAULT 0 COMMENT '访客数',
  `support_count` INT DEFAULT 0 COMMENT '支持人数',
  `support_amount` BIGINT DEFAULT 0 COMMENT '支持金额(分)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `idx_share_code` (`share_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享记录表';

-- =============================================
-- 行为日志系统
-- =============================================

-- 用户行为日志表
DROP TABLE IF EXISTS `qgc_behavior_log`;
CREATE TABLE `qgc_behavior_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `event` VARCHAR(32) NOT NULL COMMENT '事件: PAGE_VIEW/CLICK/SHARE/PAY_CLICK/CREATE/SUPPORT/COMMENT/LIKE',
  `target_id` VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
  `target_type` VARCHAR(32) DEFAULT NULL COMMENT '目标类型',
  `source` VARCHAR(32) DEFAULT NULL COMMENT '来源',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
  `device_id` VARCHAR(128) DEFAULT NULL COMMENT '设备ID',
  `extra` TEXT COMMENT '额外数据(JSON)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_event` (`event`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为日志表';

-- =============================================
-- 排行榜快照(定时任务生成)
-- =============================================

-- 排行榜快照表
DROP TABLE IF EXISTS `qgc_ranking_snapshot`;
CREATE TABLE `qgc_ranking_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ranking_type` VARCHAR(32) NOT NULL COMMENT '排行类型: SADDEST_DAY/MOST_SUPPORTERS/FASTEST_SUCCESS',
  `ranking_date` DATE NOT NULL COMMENT '排行日期',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像',
  `rank_no` INT NOT NULL COMMENT '排名',
  `score_value` BIGINT DEFAULT 0 COMMENT '分数值',
  `score_label` VARCHAR(64) DEFAULT NULL COMMENT '分数标签',
  `anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名 0否1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_date_user` (`ranking_type`, `ranking_date`, `user_id`),
  KEY `idx_ranking_type` (`ranking_type`),
  KEY `idx_ranking_date` (`ranking_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排行榜快照表';

-- =============================================
-- 随机骚话扩展分类
-- =============================================

-- 扩展随机骚话分类(更新已有表)
-- 新增分类: CREATE_SUCCESS/PAY_SUCCESS/SHARE_SUCCESS/PROOF_COMPLETE
-- 已有分类: SUPPORT/DEFAULT

-- =============================================
-- 索引优化
-- =============================================

-- campaign表补充索引(支持首页/广场查询)
ALTER TABLE `qgc_campaign` ADD INDEX `idx_status_create_time` (`status`, `create_time`);
ALTER TABLE `qgc_campaign` ADD INDEX `idx_status_end_time` (`status`, `end_time`);
ALTER TABLE `qgc_campaign` ADD INDEX `idx_status_raised` (`status`, `raised_amount`);

-- support_order表补充索引
ALTER TABLE `qgc_support_order` ADD INDEX `idx_campaign_status` (`campaign_id`, `status`);

-- notification表补充索引
ALTER TABLE `qgc_notification` ADD INDEX `idx_user_is_read` (`user_id`, `is_read`);

-- comment表补充索引
ALTER TABLE `qgc_comment` ADD INDEX `idx_target_status` (`target_id`, `target_type`, `status`);

-- qgc_user表补充索引
ALTER TABLE `qgc_user` ADD INDEX `idx_create_time` (`create_time`);

-- =============================================
-- RC2初始化数据
-- =============================================

-- 扩展广告位(RC2新增)
INSERT INTO `qgc_ad_position` (`id`, `code`, `name`, `description`, `enabled`) VALUES
(6, 'HOME_TOP', '首页顶部', '首页顶部Banner广告位', 1),
(7, 'HOME_LIST', '首页列表', '首页列表信息流广告位', 1),
(8, 'DETAIL_BOTTOM', '详情底部', '筹款详情页底部广告位', 1);

-- 扩展随机骚话(RC2新增分类)
INSERT INTO `qgc_random_message` (`content`, `category`, `enabled`) VALUES
('义父+1，距离炸鸡自由又近一步。', 'PAY_SUCCESS', 1),
('感谢义父投喂，今晚有肉吃了。', 'PAY_SUCCESS', 1),
('你的投喂已收到，穷鬼正在赶往餐厅的路上。', 'PAY_SUCCESS', 1),
('投喂成功！穷鬼感动到落泪。', 'PAY_SUCCESS', 1),
('这顿饭有你一份，下次请你吃。', 'PAY_SUCCESS', 1),
('义父出手，穷鬼不愁。', 'PAY_SUCCESS', 1),
('筹款已发起，坐等义父们投喂。', 'CREATE_SUCCESS', 1),
('穷鬼出征，寸草不生。', 'CREATE_SUCCESS', 1),
('今天也是需要投喂的一天。', 'CREATE_SUCCESS', 1),
('目标已锁定，就差你的投喂了。', 'CREATE_SUCCESS', 1),
('分享出去，让更多义父看到。', 'SHARE_SUCCESS', 1),
('每一次分享，都是穷鬼的希望。', 'SHARE_SUCCESS', 1),
('转发一下，功德+1。', 'SHARE_SUCCESS', 1),
('分享给朋友，一起投喂穷鬼。', 'SHARE_SUCCESS', 1),
('返图来了，感谢各位义父的投喂。', 'PROOF_COMPLETE', 1),
('钱已花完，饭已吃完，返图为证。', 'PROOF_COMPLETE', 1),
('投喂成果展示，请查收。', 'PROOF_COMPLETE', 1),
('这顿饭吃得很香，多亏了你们。', 'PROOF_COMPLETE', 1);

-- 系统配置(RC2新增)
INSERT INTO `qgc_system_config` (`config_key`, `config_value`, `description`) VALUES
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