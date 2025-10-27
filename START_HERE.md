# 从这里开始 🚀

## 中国大陆用户必读

如果你在中国大陆，**必须先配置Docker镜像加速器**，否则无法构建镜像。

---

## 📋 完整步骤（5分钟）

### 步骤1: 配置Docker镜像加速器（只需一次）⭐

#### Windows用户

1. **运行配置助手**（会显示详细步骤）：
```cmd
setup-docker-mirror.bat
```

2. **或者手动配置**：
   - 右键任务栏Docker图标 → **Settings**
   - 进入 **Docker Engine** 标签
   - 添加以下配置：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

   - 点击 **Apply & Restart**
   - 等待Docker重启

#### Linux用户

编辑 `/etc/docker/daemon.json`：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 步骤2: 验证配置

```cmd
docker info
```

应该看到：
```
Registry Mirrors:
  https://docker.mirrors.ustc.edu.cn/
  ...
```

### 步骤3: 运行构建脚本

**Windows**:
```cmd
docker-build-cn.bat
```

**Linux/Mac**:
```bash
chmod +x docker-build-cn.sh
./docker-build-cn.sh
```

脚本会自动检查镜像加速器配置，如果未配置会提示你。

---

## ✅ 成功标志

看到以下输出表示成功：

```
==========================================
🎉 部署成功！
==========================================

访问地址：
  📊 Web管理后台: http://localhost:18081/admin/login
  💾 H2控制台:    http://localhost:18081/h2-console
  📖 API文档:     http://localhost:18081/swagger-ui.html
```

---

## ❌ 常见错误

### 错误1: 连接超时

```
ERROR: failed to fetch anonymous token
```

**解决**: 未配置镜像加速器，回到步骤1

### 错误2: not found

```
ERROR: not found
```

**解决**: 镜像加速器配置未生效
1. 确认配置正确
2. 确认Docker已重启
3. 运行 `docker info` 验证

### 错误3: Maven构建失败

```
❌ Maven构建失败
```

**解决**: 
```cmd
mvn clean install -DskipTests -U
```

---

## 📁 脚本说明

| 脚本 | 用途 | 何时使用 |
|------|------|----------|
| **setup-docker-mirror.bat** | 配置助手 | 首次使用 |
| **docker-build-cn.bat** | 国内优化构建 ⭐ | 已配置镜像加速器 |
| **docker-build.bat** | 标准构建 | 已配置镜像加速器 |

**推荐**: 使用 `docker-build-cn.bat`，它使用阿里云Alpine软件源，`apk`安装更快。

---

## 🎯 快速命令

```cmd
REM 1. 首次使用 - 配置镜像加速器
setup-docker-mirror.bat

REM 2. 验证配置
docker info

REM 3. 构建和部署
docker-build-cn.bat

REM 4. 查看日志
docker-compose logs -f excel-api

REM 5. 停止服务
docker-compose down
```

---

## 📚 详细文档

- [QUICK_FIX.md](QUICK_FIX.md) - 快速问题解决
- [DOCKER_NETWORK_FIX.md](DOCKER_NETWORK_FIX.md) - 完整网络问题指南
- [DOCKERFILE_OPTIMIZATION.md](DOCKERFILE_OPTIMIZATION.md) - Docker优化详解
- [README.md](README.md) - 完整项目文档

---

## 💡 提示

1. **镜像加速器只需配置一次**，以后所有Docker镜像都会加速
2. 配置后所有Docker Hub镜像都能正常访问
3. 推荐使用 `docker-build-cn.bat`，Alpine软件源也使用国内镜像

---

**总结**: 配置镜像加速器 → 运行 `docker-build-cn.bat` → 完成！🎉

