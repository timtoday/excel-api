# 使用轻量级的 Alpine JRE 镜像（约180MB）
FROM eclipse-temurin:17-jre-alpine

# 设置工作目录
WORKDIR /app

# 创建必要的目录
RUN mkdir -p /data/excel-files /data/excel-temp /app/logs

# 安装健康检查所需的wget（Alpine默认没有）
RUN apk add --no-cache wget

# 复制本地构建好的jar文件
COPY target/excel-api-*.jar app.jar

# 暴露端口
EXPOSE 18081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:18081/api/excel/health || exit 1

# 启动应用（优化JVM参数）
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

