#!/bin/bash

# Excel API 启动脚本

echo "================================================"
echo "    Excel API Service - 启动脚本"
echo "================================================"
echo ""

# 检查Java版本
echo "检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "❌ 错误：未找到Java，请安装JDK 17或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ 错误：需要Java 17或更高版本，当前版本：$JAVA_VERSION"
    exit 1
fi

echo "✅ Java版本检查通过"
echo ""

# 检查Maven
echo "检查Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误：未找到Maven，请安装Maven 3.6或更高版本"
    exit 1
fi

echo "✅ Maven检查通过"
echo ""

# 创建必要的目录
echo "创建存储目录..."
mkdir -p excel-files
mkdir -p excel-temp
mkdir -p logs
echo "✅ 目录创建完成"
echo ""

# 选择启动模式
echo "请选择启动模式："
echo "1) 开发模式（本地锁，热重载）"
echo "2) 生产模式（需要Redis）"
echo "3) Docker模式"
read -p "请输入选项 [1-3]: " mode

case $mode in
    1)
        echo ""
        echo "🚀 启动开发模式..."
        echo ""
        mvn spring-boot:run -Dspring-boot.run.profiles=default
        ;;
    2)
        echo ""
        echo "⚠️  生产模式需要Redis服务"
        read -p "Redis已经启动了吗？(y/n): " redis_ready
        if [ "$redis_ready" != "y" ]; then
            echo "请先启动Redis服务"
            exit 1
        fi
        echo ""
        echo "🚀 启动生产模式..."
        echo ""
        mvn spring-boot:run -Dspring-boot.run.profiles=prod
        ;;
    3)
        echo ""
        echo "🐳 使用Docker启动..."
        if ! command -v docker-compose &> /dev/null; then
            echo "❌ 错误：未找到docker-compose"
            exit 1
        fi
        echo ""
        docker-compose up -d
        echo ""
        echo "✅ Docker容器已启动"
        echo "查看日志: docker-compose logs -f"
        echo "停止服务: docker-compose down"
        ;;
    *)
        echo "❌ 无效的选项"
        exit 1
        ;;
esac

if [ $mode -ne 3 ]; then
    echo ""
    echo "================================================"
    echo "    服务已启动！"
    echo "================================================"
    echo ""
    echo "📋 访问地址："
    echo "  - API文档: http://localhost:8080/swagger-ui.html"
    echo "  - 健康检查: http://localhost:8080/api/excel/health"
    echo ""
    echo "📁 文件存储："
    echo "  - Excel文件: ./excel-files/"
    echo "  - 临时文件: ./excel-temp/"
    echo "  - 日志文件: ./logs/"
    echo ""
    echo "📖 使用文档："
    echo "  - 快速开始: QUICK_START.md"
    echo "  - API示例: API_EXAMPLES.md"
    echo "  - 完整文档: README.md"
    echo ""
    echo "按 Ctrl+C 停止服务"
    echo "================================================"
fi

