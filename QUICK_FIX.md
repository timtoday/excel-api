# 快速修复：Docker网络超时问题 ⚡

## 错误信息

```
ERROR: failed to fetch anonymous token: Get "https://auth.docker.io/token...": 
dial tcp 162.125.32.13:443: connectex: A connection attempt failed...
```

或

```
ERROR: failed to resolve source metadata: not found
```

---

## 🚀 立即解决（2分钟）

### 步骤1: 配置Docker镜像加速器（必须）

**Windows用户** - 运行配置助手：

```cmd
setup-docker-mirror.bat
```

按照提示操作：
1. 右键任务栏Docker图标 → Settings
2. 进入 Docker Engine 标签
3. 添加镜像配置（脚本会显示）
4. 点击 Apply & Restart

**或者手动配置** - 在Docker Engine中添加：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
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

### 步骤3: 重新构建

```cmd
docker-build.bat
```

---

## 📋 手动执行（如果脚本失败）

```bash
# 1. Maven构建
mvn clean package -DskipTests

# 2. 使用国内镜像构建Docker
docker build -f Dockerfile.cn -t excel-api:latest .

# 3. 启动服务
docker-compose up -d

# 4. 验证
curl http://localhost:18081/api/excel/health
```

---

## 🔧 永久解决（可选，建议配置）

### Windows (Docker Desktop)

1. 右键任务栏Docker图标 → **Settings**
2. 进入 **Docker Engine** 标签
3. 添加以下配置：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}
```

4. 点击 **Apply & Restart**

配置完成后，以后使用原始的 `docker-build.bat` 也能正常工作。

### Linux

```bash
# 编辑Docker配置
sudo nano /etc/docker/daemon.json

# 添加以下内容
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com",
    "https://mirror.ccs.tencentyun.com"
  ]
}

# 重启Docker
sudo systemctl daemon-reload
sudo systemctl restart docker
```

---

## 📁 文件说明

| 文件 | 用途 | 何时使用 |
|------|------|----------|
| **docker-build.bat** | 国际版本（原始） | 配置镜像加速器后 |
| **docker-build-cn.bat** | 国内优化版本 ⭐ | 中国大陆用户推荐 |
| **Dockerfile** | 国际版本 | 使用Docker Hub镜像 |
| **Dockerfile.cn** | 国内优化版本 ⭐ | 使用阿里云镜像 |

---

## ✅ 验证

构建成功后，访问：

- http://localhost:18081/admin/login
- 用户名：`admin`
- 密码：`admin123`

---

## 📚 详细文档

如需深入了解，请查看：
- [DOCKER_NETWORK_FIX.md](DOCKER_NETWORK_FIX.md) - 完整的网络问题解决方案
- [DOCKERFILE_OPTIMIZATION.md](DOCKERFILE_OPTIMIZATION.md) - Docker优化说明

---

**TL;DR**: 直接运行 `docker-build-cn.bat` (Windows) 或 `./docker-build-cn.sh` (Linux/Mac) 🎉

