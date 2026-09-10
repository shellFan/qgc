#!/bin/bash
# =============================================
# 穷鬼筹 一键部署脚本
# 用法: 在项目根目录执行 bash deploy/deploy.sh
# 前提: 已构建前端(H5+Admin)和后端(mvn package)
# =============================================

set -e

# 配置
SERVER_APP_DIR="/opt/qgc/app"
SERVER_H5_DIR="/opt/qgc/h5"
SERVER_ADMIN_DIR="/opt/qgc/admin"
SERVER_UPLOADS_DIR="/opt/qgc/uploads"
SERVER_NGINX_CONF="/www/server/panel/vhost/nginx/pay.21zuo.com.conf"
JAR_NAME="qgc-server-main-1.0.0.jar"
SERVICE_NAME="qgc-server"

echo "========================================="
echo "  穷鬼筹 部署脚本"
echo "========================================="

# 1. 创建必要目录
echo "[1/8] 创建目录..."
mkdir -p ${SERVER_APP_DIR}
mkdir -p ${SERVER_H5_DIR}
mkdir -p ${SERVER_ADMIN_DIR}
mkdir -p ${SERVER_UPLOADS_DIR}

# 2. 停止服务
echo "[2/8] 停止 ${SERVICE_NAME} 服务..."
systemctl stop ${SERVICE_NAME} 2>/dev/null || echo "服务未运行，跳过"

# 3. 备份当前jar
echo "[3/8] 备份当前版本..."
if [ -f "${SERVER_APP_DIR}/${JAR_NAME}" ]; then
    cp ${SERVER_APP_DIR}/${JAR_NAME} ${SERVER_APP_DIR}/${JAR_NAME}.bak.$(date +%Y%m%d%H%M%S)
fi

# 4. 部署后端jar
echo "[4/8] 部署后端..."
cp qgc-server/qgc-server-main/target/${JAR_NAME} ${SERVER_APP_DIR}/${JAR_NAME}
echo "  jar已部署到 ${SERVER_APP_DIR}/${JAR_NAME}"

# 5. 部署H5前端
echo "[5/8] 部署H5前端..."
rm -rf ${SERVER_H5_DIR}/*
cp -r qgc-h5/dist/* ${SERVER_H5_DIR}/
echo "  H5已部署到 ${SERVER_H5_DIR}"

# 6. 部署Admin前端
echo "[6/8] 部署Admin前端..."
rm -rf ${SERVER_ADMIN_DIR}/*
cp -r qgc-admin-web/dist/* ${SERVER_ADMIN_DIR}/
echo "  Admin已部署到 ${SERVER_ADMIN_DIR}"

# 7. 执行RC7数据库迁移
echo "[7/8] 执行RC7数据库迁移..."
# 读取数据库配置
DB_HOST=${SPRING_DATASOURCE_HOST:-localhost}
DB_PORT=${SPRING_DATASOURCE_PORT:-3306}
DB_NAME=${SPRING_DATASOURCE_DB:-qiongguichou}
DB_USER=${SPRING_DATASOURCE_USERNAME:-qiongguichou}
DB_PASS=${SPRING_DATASOURCE_PASSWORD:-qiongguichou_pass_2024}

mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} ${DB_NAME} < sql/09_rc7_schema.sql && echo "  RC7迁移完成" || echo "  RC7迁移失败(可能已执行过)"

# 8. 更新Nginx配置并重启
echo "[8/8] 更新Nginx配置..."
cp deploy/pay.21zuo.com.nginx.conf ${SERVER_NGINX_CONF}
nginx -t && nginx -s reload && echo "  Nginx配置已更新并重载"

# 启动服务
echo ""
echo "启动 ${SERVICE_NAME} 服务..."
systemctl start ${SERVICE_NAME}
sleep 3
systemctl status ${SERVICE_NAME} --no-pager | head -5

echo ""
echo "========================================="
echo "  部署完成!"
echo "========================================="
echo ""
echo "  首页地址: https://pay.21zuo.com/h5/"
echo "  后台管理: https://pay.21zuo.com/admin/web/"
echo "  管理员账号: admin"
echo "  管理员密码: Admin@123456"
echo ""