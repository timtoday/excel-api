@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ================================================
echo     Excel API Service - 启动脚本
echo ================================================
echo.

REM 检查Java
echo 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未找到Java，请安装JDK 17或更高版本
    pause
    exit /b 1
)
echo ✅ Java版本检查通过
echo.

REM 检查Maven
echo 检查Maven...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未找到Maven，请安装Maven 3.6或更高版本
    pause
    exit /b 1
)
echo ✅ Maven检查通过
echo.

REM 检查Docker和Redis
echo 检查Redis服务...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ⚠️  警告：Docker未安装或未运行
    echo    本地开发需要Redis服务
    echo    请手动启动Redis或安装Docker
    echo.
) else (
    REM 检查Redis容器状态
    docker ps | findstr excel-redis >nul 2>&1
    if errorlevel 1 (
        echo ⚠️  Redis容器未运行，正在启动...
        docker-compose up -d redis
        if errorlevel 0 (
            echo ✅ Redis已启动
            timeout /t 3 /nobreak >nul
        ) else (
            echo ❌ Redis启动失败
            echo    请手动运行: docker-compose up -d redis
        )
    ) else (
        echo ✅ Redis服务运行正常
    )
)
echo.

REM 创建必要的目录
echo 创建存储目录...
if not exist "excel-files" mkdir excel-files
if not exist "excel-temp" mkdir excel-temp
if not exist "logs" mkdir logs
echo ✅ 目录创建完成
echo.

REM 选择启动模式
echo 请选择启动模式：
echo 1) 开发模式（本地锁）
echo 2) 生产模式（需要Redis）
echo 3) Docker模式
set /p mode="请输入选项 [1-3]: "

if "%mode%"=="1" (
    echo.
    echo 🚀 启动开发模式...
    echo.
    mvn spring-boot:run -Dspring-boot.run.profiles=default
) else if "%mode%"=="2" (
    echo.
    echo ⚠️  生产模式需要Redis服务
    set /p redis_ready="Redis已经启动了吗？(y/n): "
    if "!redis_ready!" neq "y" (
        echo 请先启动Redis服务
        pause
        exit /b 1
    )
    echo.
    echo 🚀 启动生产模式...
    echo.
    mvn spring-boot:run -Dspring-boot.run.profiles=prod
) else if "%mode%"=="3" (
    echo.
    echo 🐳 使用Docker启动...
    docker-compose --version >nul 2>&1
    if errorlevel 1 (
        echo ❌ 错误：未找到docker-compose
        pause
        exit /b 1
    )
    echo.
    docker-compose up -d
    echo.
    echo ✅ Docker容器已启动
    echo 查看日志: docker-compose logs -f
    echo 停止服务: docker-compose down
    pause
    exit /b 0
) else (
    echo ❌ 无效的选项
    pause
    exit /b 1
)

echo.
echo ================================================
echo     服务已启动！
echo ================================================
echo.
echo 📋 访问地址：
echo   - API文档: http://localhost:8080/swagger-ui.html
echo   - 健康检查: http://localhost:8080/api/excel/health
echo.
echo 📁 文件存储：
echo   - Excel文件: .\excel-files\
echo   - 临时文件: .\excel-temp\
echo   - 日志文件: .\logs\
echo.
echo 📖 使用文档：
echo   - 快速开始: QUICK_START.md
echo   - API示例: API_EXAMPLES.md
echo   - 完整文档: README.md
echo.
echo 按 Ctrl+C 停止服务
echo ================================================

pause

