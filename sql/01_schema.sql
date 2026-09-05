-- =============================================
-- 穷鬼筹 V1.0 数据库建表脚本
-- 兼容 MySQL 5.6
-- =============================================
CREATE DATABASE IF NOT EXISTS `qiongguichou` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `qiongguichou`;

-- ---------------------------------------------
-- 用户表
-- ---------------------------------------------
CREATE TABLE `qgc_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL COMMENT '微信openid',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信unionid',
  `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `sex` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `country` VARCHAR(64) DEFAULT NULL COMMENT '国家',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
  `subscribe` TINYINT DEFAULT 0 COMMENT '是否关注公众号 0否 1是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 2封禁',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除 0正常 1删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ---------------------------------------------
-- 用户钱包表
-- ---------------------------------------------
CREATE TABLE `qgc_user_wallet` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `balance` BIGINT NOT NULL DEFAULT 0 COMMENT '可提现余额(分)',
  `frozen_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '冻结金额(分)',
  `total_income` BIGINT NOT NULL DEFAULT 0 COMMENT '累计收入(分)',
  `total_withdraw` BIGINT NOT NULL DEFAULT 0 COMMENT '累计提现(分)',
  `total_support` BIGINT NOT NULL DEFAULT 0 COMMENT '累计投喂别人(分)',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包表';

-- ---------------------------------------------
-- 钱包流水表
-- ---------------------------------------------
CREATE TABLE `qgc_wallet_flow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `flow_no` VARCHAR(64) NOT NULL COMMENT '流水号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '流水类型: CAMPAIGN_INCOME/WITHDRAW_APPLY/WITHDRAW_SUCCESS/WITHDRAW_FAIL_RETURN/PAYMENT_REFUND/WECHAT_FEE/ADMIN_ADJUST',
  `amount` BIGINT NOT NULL COMMENT '变动金额(分),正为入,负为出',
  `balance_after` BIGINT NOT NULL COMMENT '变动后余额(分)',
  `related_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
  `related_type` VARCHAR(32) DEFAULT NULL COMMENT '关联业务类型',
  `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_flow_no` (`flow_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水表';

-- ---------------------------------------------
-- 筹款分类表
-- ---------------------------------------------
CREATE TABLE `qgc_campaign_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(256) DEFAULT NULL COMMENT '图标',
  `cover` VARCHAR(512) DEFAULT NULL COMMENT '封面图',
  `theme_color` VARCHAR(16) DEFAULT NULL COMMENT '主题色',
  `default_title` VARCHAR(128) DEFAULT NULL COMMENT '默认标题模板',
  `default_description` VARCHAR(512) DEFAULT NULL COMMENT '默认描述模板',
  `share_title_template` VARCHAR(128) DEFAULT NULL COMMENT '分享标题模板',
  `share_desc_template` VARCHAR(256) DEFAULT NULL COMMENT '分享描述模板',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序(越小越前)',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用 0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筹款分类表';

-- ---------------------------------------------
-- 筹款项目表
-- ---------------------------------------------
CREATE TABLE `qgc_campaign` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `campaign_no` VARCHAR(64) NOT NULL COMMENT '项目编号',
  `creator_user_id` BIGINT NOT NULL COMMENT '发起人用户ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `description` TEXT NOT NULL COMMENT '介绍',
  `cover` VARCHAR(512) NOT NULL COMMENT '封面图URL',
  `target_amount` BIGINT NOT NULL COMMENT '目标金额(分)',
  `raised_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '已筹金额(分)',
  `support_count` INT NOT NULL DEFAULT 0 COMMENT '支持人数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `duration_hours` INT NOT NULL COMMENT '筹款时长(小时)',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `visibility` VARCHAR(16) NOT NULL DEFAULT 'PUBLIC' COMMENT '公开范围: PUBLIC/PRIVATE_LINK',
  `allow_ranking` TINYINT NOT NULL DEFAULT 1 COMMENT '允许排行榜 0否 1是',
  `allow_comment` TINYINT NOT NULL DEFAULT 1 COMMENT '允许评论 0否 1是',
  `status` VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING_REVIEW/ACTIVE/SUCCESS/EXPIRED/CLOSED/REJECTED/RISK_FROZEN',
  `reject_reason` VARCHAR(512) DEFAULT NULL COMMENT '拒绝原因',
  `close_reason` VARCHAR(512) DEFAULT NULL COMMENT '关闭原因',
  `proof_status` TINYINT NOT NULL DEFAULT 0 COMMENT '返图状态 0未返图 1已返图',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_campaign_no` (`campaign_no`),
  KEY `idx_creator_user_id` (`creator_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筹款项目表';

-- ---------------------------------------------
-- 筹款项目图片表
-- ---------------------------------------------
CREATE TABLE `qgc_campaign_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `campaign_id` BIGINT NOT NULL COMMENT '筹款项目ID',
  `image_url` VARCHAR(512) NOT NULL COMMENT '图片URL',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_campaign_id` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筹款项目图片表';

-- ---------------------------------------------
-- 支持订单表
-- ---------------------------------------------
CREATE TABLE `qgc_support_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `support_no` VARCHAR(64) NOT NULL COMMENT '支持订单号',
  `campaign_id` BIGINT NOT NULL COMMENT '筹款项目ID',
  `supporter_user_id` BIGINT NOT NULL COMMENT '支持者用户ID',
  `creator_user_id` BIGINT NOT NULL COMMENT '发起人用户ID',
  `amount` BIGINT NOT NULL COMMENT '支持金额(分)',
  `effective_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '有效金额(分)',
  `refund_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '退款金额(分)',
  `anonymous` TINYINT NOT NULL DEFAULT 0 COMMENT '是否匿名 0否 1是',
  `hide_amount` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏金额 0否 1是',
  `message` VARCHAR(256) DEFAULT NULL COMMENT '留言',
  `status` VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '状态: CREATED/PAID/REFUNDED/PART_REFUNDED/CLOSED',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_support_no` (`support_no`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `idx_supporter_user_id` (`supporter_user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支持订单表';

-- ---------------------------------------------
-- 支付订单表
-- ---------------------------------------------
CREATE TABLE `qgc_payment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL COMMENT '支付订单号',
  `support_no` VARCHAR(64) DEFAULT NULL COMMENT '关联支持订单号',
  `campaign_id` BIGINT NOT NULL COMMENT '筹款项目ID',
  `supporter_user_id` BIGINT NOT NULL COMMENT '支持者用户ID',
  `creator_user_id` BIGINT NOT NULL COMMENT '发起人用户ID',
  `amount` BIGINT NOT NULL COMMENT '支付金额(分)',
  `effective_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '有效金额(分)',
  `refund_amount` BIGINT NOT NULL DEFAULT 0 COMMENT '退款金额(分)',
  `trade_type` VARCHAR(32) NOT NULL DEFAULT 'JSAPI' COMMENT '交易类型',
  `openid` VARCHAR(64) NOT NULL COMMENT '支付者openid',
  `prepay_id` VARCHAR(64) DEFAULT NULL COMMENT '预支付ID',
  `transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付流水号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT '状态: CREATED/PAYING/SUCCESS/CLOSED/REFUNDING/PART_REFUNDED/REFUNDED/FAIL',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `notify_time` DATETIME DEFAULT NULL COMMENT '回调时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `idx_supporter_user_id` (`supporter_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- ---------------------------------------------
-- 退款订单表
-- ---------------------------------------------
CREATE TABLE `qgc_refund_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
  `payment_order_no` VARCHAR(64) NOT NULL COMMENT '关联支付订单号',
  `campaign_id` BIGINT NOT NULL COMMENT '筹款项目ID',
  `user_id` BIGINT NOT NULL COMMENT '退款用户ID',
  `amount` BIGINT NOT NULL COMMENT '退款金额(分)',
  `reason` VARCHAR(256) DEFAULT NULL COMMENT '退款原因',
  `type` VARCHAR(32) NOT NULL DEFAULT 'OVERPAY' COMMENT '退款类型: OVERPAY/ADMIN/CLOSED',
  `wechat_refund_id` VARCHAR(64) DEFAULT NULL COMMENT '微信退款单号',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/SUCCESS/FAIL',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款成功时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_payment_order_no` (`payment_order_no`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款订单表';

-- ---------------------------------------------
-- 提现订单表
-- ---------------------------------------------
CREATE TABLE `qgc_withdraw_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `withdraw_no` VARCHAR(64) NOT NULL COMMENT '提现单号',
  `user_id` BIGINT NOT NULL COMMENT '提现用户ID',
  `amount` BIGINT NOT NULL COMMENT '提现金额(分)',
  `fee` BIGINT NOT NULL DEFAULT 0 COMMENT '手续费(分)',
  `actual_amount` BIGINT NOT NULL COMMENT '实际到账(分)',
  `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/SUCCESS/FAIL/REJECTED',
  `reject_reason` VARCHAR(256) DEFAULT NULL COMMENT '拒绝原因',
  `transfer_no` VARCHAR(64) DEFAULT NULL COMMENT '转账流水号',
  `transfer_time` DATETIME DEFAULT NULL COMMENT '转账时间',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现订单表';

-- ---------------------------------------------
-- 返图表
-- ---------------------------------------------
CREATE TABLE `qgc_proof` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `campaign_id` BIGINT NOT NULL COMMENT '筹款项目ID',
  `user_id` BIGINT NOT NULL COMMENT '发起人用户ID',
  `title` VARCHAR(128) NOT NULL COMMENT '返图标题',
  `content` TEXT NOT NULL COMMENT '返图内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_campaign_id` (`campaign_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返图表';

-- ---------------------------------------------
-- 返图图片表
-- ---------------------------------------------
CREATE TABLE `qgc_proof_image` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `proof_id` BIGINT NOT NULL COMMENT '返图ID',
  `image_url` VARCHAR(512) NOT NULL COMMENT '图片URL',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_proof_id` (`proof_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返图图片表';

-- ---------------------------------------------
-- 返图点赞表
-- ---------------------------------------------
CREATE TABLE `qgc_proof_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `proof_id` BIGINT NOT NULL COMMENT '返图ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_proof_user` (`proof_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='返图点赞表';

-- ---------------------------------------------
-- 评论表
-- ---------------------------------------------
CREATE TABLE `qgc_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_id` BIGINT NOT NULL COMMENT '目标ID(筹款ID或返图ID)',
  `target_type` VARCHAR(16) NOT NULL COMMENT '目标类型: CAMPAIGN/PROOF',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `content` VARCHAR(512) NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 2隐藏 3删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ---------------------------------------------
-- 举报表
-- ---------------------------------------------
CREATE TABLE `qgc_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reporter_user_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `target_type` VARCHAR(16) NOT NULL COMMENT '目标类型: CAMPAIGN/COMMENT/USER',
  `reason_type` VARCHAR(32) NOT NULL COMMENT '原因: FAKE/PORN/ILLEGAL/FRAUD/ADS/ATTACK/OTHER',
  `reason_desc` VARCHAR(512) DEFAULT NULL COMMENT '补充说明',
  `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/CONFIRMED/IGNORED',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handle_remark` VARCHAR(256) DEFAULT NULL COMMENT '处理备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`, `target_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- ---------------------------------------------
-- 敏感词表
-- ---------------------------------------------
CREATE TABLE `qgc_sensitive_word` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `word` VARCHAR(64) NOT NULL COMMENT '敏感词',
  `category` VARCHAR(32) DEFAULT NULL COMMENT '分类',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

-- ---------------------------------------------
-- 广告位表
-- ---------------------------------------------
CREATE TABLE `qgc_ad_position` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT '广告位编码: HOME_FEED/CAMPAIGN_DETAIL_BOTTOM/PAY_SUCCESS/PROOF_PAGE/CREATE_SUCCESS',
  `name` VARCHAR(64) NOT NULL COMMENT '广告位名称',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '描述',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告位表';

-- ---------------------------------------------
-- 广告表
-- ---------------------------------------------
CREATE TABLE `qgc_ad` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `position_id` BIGINT NOT NULL COMMENT '广告位ID',
  `title` VARCHAR(128) NOT NULL COMMENT '广告标题',
  `image_url` VARCHAR(512) NOT NULL COMMENT '广告图片URL',
  `link_url` VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_position_id` (`position_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告表';

-- ---------------------------------------------
-- 广告统计表
-- ---------------------------------------------
CREATE TABLE `qgc_ad_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ad_id` BIGINT NOT NULL COMMENT '广告ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `pv` INT NOT NULL DEFAULT 0 COMMENT '曝光量',
  `click` INT NOT NULL DEFAULT 0 COMMENT '点击量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ad_date` (`ad_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告统计表';

-- ---------------------------------------------
-- 分享模板表
-- ---------------------------------------------
CREATE TABLE `qgc_share_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID,NULL为通用',
  `title_template` VARCHAR(128) NOT NULL COMMENT '分享标题模板',
  `desc_template` VARCHAR(256) NOT NULL COMMENT '分享描述模板',
  `enabled` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享模板表';

-- ---------------------------------------------
-- 随机骚话表
-- ---------------------------------------------
CREATE TABLE `qgc_random_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `content` VARCHAR(256) NOT NULL COMMENT '骚话内容',
  `category` VARCHAR(32) DEFAULT 'DEFAULT' COMMENT '分类: DEFAULT/SUPPORT/SHARE',
  `enabled` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='随机骚话表';

-- ---------------------------------------------
-- 消息通知表
-- ---------------------------------------------
CREATE TABLE `qgc_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '类型: SUPPORT/SUCCESS/EXPIRE/COMMENT/LIKE/WITHDRAW/REVIEW/ADMIN',
  `title` VARCHAR(128) NOT NULL COMMENT '标题',
  `content` VARCHAR(512) DEFAULT NULL COMMENT '内容',
  `related_id` BIGINT DEFAULT NULL COMMENT '关联ID',
  `related_type` VARCHAR(32) DEFAULT NULL COMMENT '关联类型',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- ---------------------------------------------
-- 风控记录表
-- ---------------------------------------------
CREATE TABLE `qgc_risk_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
  `action` VARCHAR(32) NOT NULL COMMENT '动作: CREATE_CAMPAIGN/PAY/WITHDRAW/REPORT',
  `risk_type` VARCHAR(32) NOT NULL COMMENT '风控类型: FREQUENCY/DUPLICATE/ABNORMAL',
  `detail` VARCHAR(512) DEFAULT NULL COMMENT '详情',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控记录表';

-- ---------------------------------------------
-- 黑名单表
-- ---------------------------------------------
CREATE TABLE `qgc_blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` VARCHAR(16) NOT NULL COMMENT '类型: USER/IP',
  `target_value` VARCHAR(128) NOT NULL COMMENT '值',
  `reason` VARCHAR(256) DEFAULT NULL COMMENT '原因',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_value` (`target_type`, `target_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';

-- ---------------------------------------------
-- 系统配置表
-- ---------------------------------------------
CREATE TABLE `qgc_system_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ---------------------------------------------
-- 管理员表
-- ---------------------------------------------
CREATE TABLE `qgc_admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL COMMENT '用户名',
  `password` VARCHAR(128) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1正常 2禁用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `must_change_password` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要修改密码',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ---------------------------------------------
-- 角色表
-- ---------------------------------------------
CREATE TABLE `qgc_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_code` VARCHAR(32) NOT NULL COMMENT '角色编码: SUPER_ADMIN/OPERATOR/AUDITOR/FINANCE',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `description` VARCHAR(256) DEFAULT NULL COMMENT '描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ---------------------------------------------
-- 管理员角色关联表
-- ---------------------------------------------
CREATE TABLE `qgc_admin_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员角色关联表';

-- ---------------------------------------------
-- 管理员操作日志表
-- ---------------------------------------------
CREATE TABLE `qgc_admin_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `admin_username` VARCHAR(64) DEFAULT NULL COMMENT '管理员用户名',
  `action` VARCHAR(64) NOT NULL COMMENT '操作',
  `target_type` VARCHAR(32) DEFAULT NULL COMMENT '目标类型',
  `target_id` VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
  `before_data` TEXT DEFAULT NULL COMMENT '操作前数据',
  `after_data` TEXT DEFAULT NULL COMMENT '操作后数据',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT 'IP',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';