-- =============================================
-- 穷鬼筹 V1.0 管理员初始化
-- =============================================
USE `qiongguichou`;

-- 角色
INSERT INTO `qgc_role` (`id`, `role_code`, `role_name`, `description`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', '拥有全部权限'),
(2, 'OPERATOR', '运营', '运营相关权限'),
(3, 'AUDITOR', '审核员', '内容审核权限'),
(4, 'FINANCE', '财务', '提现、支付、退款权限');

-- 管理员(密码: Admin@123456, BCrypt加密)
-- BCrypt hash for 'Admin@123456'
INSERT INTO `qgc_admin` (`id`, `username`, `password`, `nickname`, `status`, `must_change_password`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1);

-- 管理员角色关联
INSERT INTO `qgc_admin_role` (`admin_id`, `role_id`) VALUES
(1, 1);