@echo off
chcp 65001 >nul
REM Docker构建和部署脚本 (Windows)

echo ==========================================
echo Excel API Docker 构建和部署
echo ==========================================
echo.

REM 1. Maven构建
echo 📦 步骤 1/4: Maven构建jar包...
echo.
echo 💡 使用标准构建方式（推荐）
call mvn clean package -DskipTests
REM 如果想使用Maven Profile，可以改为：
REM call mvn -P prod clean package -DskipTests 
if %errorlevel% neq 0 (
    echo ❌ Maven构建失败
    exit /b %errorlevel%
)
echo ✅ Maven构建完成
echo.

REM 2. 构建Docker镜像
echo 🐳 步骤 2/4: 构建Docker镜像...
docker build -t excel-api:latest .
if %errorlevel% neq 0 (
    echo ❌ Docker镜像构建失败
    exit /b %errorlevel%
)
echo ✅ Docker镜像构建完成
echo.

REM 3. 查看镜像大小
echo 📊 步骤 3/4: 镜像信息
docker images excel-api:latest
echo.

REM 4. 启动服务
echo 🚀 步骤 4/4: 启动Docker Compose服务...
docker-compose down
docker-compose up -d
if %errorlevel% neq 0 (
    echo ❌ 服务启动失败
    exit /b %errorlevel%
)
echo ✅ 服务启动完成
echo.

REM 5. 等待服务就绪
echo ⏳ 等待服务启动...
timeout /t 5 /nobreak >nul

REM 6. 健康检查
echo 🏥 健康检查...
curl -s http://localhost:18081/api/excel/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 服务运行正常
) else (
    echo ❌ 服务启动失败，请查看日志
    docker-compose logs excel-api
    exit /b 1
)

echo.
echo ==========================================
echo 🎉 部署成功！
echo ==========================================
echo.
echo 访问地址：
echo   📊 Web管理后台: http://localhost:18081/admin/login
echo   💾 H2控制台:    http://localhost:18081/h2-console
echo   📖 API文档:     http://localhost:18081/swagger-ui.html
echo.
echo 查看日志：
echo   docker-compose logs -f excel-api
echo.
pause

