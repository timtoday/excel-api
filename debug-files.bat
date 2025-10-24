@echo off
chcp 65001 >nul
echo ====================================
echo Excel API - 文件上传调试工具
echo ====================================
echo.

echo 1. 检查目录结构
echo ====================================
if exist "excel-files" (
    echo [√] excel-files 目录存在
    dir /b "excel-files"
    echo.
    echo 文件详情:
    dir "excel-files"
) else (
    echo [×] excel-files 目录不存在
)
echo.

if exist "excel-temp" (
    echo [√] excel-temp 目录存在
) else (
    echo [×] excel-temp 目录不存在
)
echo.

if exist "logs" (
    echo [√] logs 目录存在
) else (
    echo [×] logs 目录不存在
)
echo.

echo 2. 检查日志文件
echo ====================================
if exist "logs\excel-api.log" (
    echo [√] 日志文件存在
    echo 最后 20 行日志:
    echo --------
    powershell -Command "Get-Content logs\excel-api.log -Tail 20"
) else (
    echo [×] 日志文件不存在
)
echo.

echo 3. 测试上传文件
echo ====================================
echo 请打开浏览器访问: http://localhost:8080/admin/files
echo 上传一个Excel文件后，再次运行此脚本查看结果
echo.

pause

