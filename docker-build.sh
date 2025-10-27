#!/bin/bash
# Docker构建和部署脚本

set -e

echo "=========================================="
echo "Excel API Docker 构建和部署"
echo "=========================================="
echo ""

# 1. Maven构建
echo "📦 步骤 1/4: Maven构建jar包..."
mvn clean package -DskipTests
echo "✅ Maven构建完成"
echo ""

# 2. 构建Docker镜像
echo "🐳 步骤 2/4: 构建Docker镜像..."
docker build -t excel-api:latest .
echo "✅ Docker镜像构建完成"
echo ""

# 3. 查看镜像大小
echo "📊 步骤 3/4: 镜像信息"
docker images excel-api:latest
echo ""

# 4. 启动服务
echo "🚀 步骤 4/4: 启动Docker Compose服务..."
docker-compose down
docker-compose up -d
echo "✅ 服务启动完成"
echo ""

# 5. 等待服务就绪
echo "⏳ 等待服务启动..."
sleep 5

# 6. 健康检查
echo "🏥 健康检查..."
if curl -s http://localhost:18081/api/excel/health > /dev/null; then
    echo "✅ 服务运行正常"
else
    echo "❌ 服务启动失败，请查看日志"
    docker-compose logs excel-api
    exit 1
fi

echo ""
echo "=========================================="
echo "🎉 部署成功！"
echo "=========================================="
echo ""
echo "访问地址："
echo "  📊 Web管理后台: http://localhost:18081/admin/login"
echo "  💾 H2控制台:    http://localhost:18081/h2-console"
echo "  📖 API文档:     http://localhost:18081/swagger-ui.html"
echo ""
echo "查看日志："
echo "  docker-compose logs -f excel-api"
echo ""

