# 云部署流程

本文档说明项目未来部署到云服务器时的推荐流程。当前阶段先按简单可靠的方式建设，不一开始引入过多平台复杂度。

## 推荐架构

基础云部署结构：

```text
用户 / 手机 App / Web
        ↓
HTTPS 域名
        ↓
Nginx
        ↓
Spring Boot API
        ↓
MySQL / Redis
```

推荐域名规划：

```text
api-test.example.com   测试后端
api.example.com        生产后端
web-test.example.com   测试 Web
web.example.com        生产 Web
```

## 服务器准备

最低配置建议：

```text
CPU：2 核
内存：4G
磁盘：40G+
系统：Ubuntu LTS / Debian / CentOS Stream
```

生产建议：

- 数据库不要和应用长期混在同一台小机器上。
- 开启云厂商安全组，只暴露必要端口。
- 使用 HTTPS。
- 配置日志轮转。
- 定期备份数据库。

## 后端部署方式

### 方式一：Jar 部署

本地或 CI 构建：

```bash
cd production-sales-inventory-api
mvn -DskipTests package
```

产物：

```text
production-sales-inventory-api/target/*.jar
```

服务器启动：

```bash
java -jar production-sales-inventory-api.jar
```

建议用 systemd 管理进程。

### 方式二：Docker 部署

后续可以把 Spring Boot 打成 Docker 镜像：

```text
production-sales-inventory-api
        ↓
Docker image
        ↓
云服务器 / 容器服务
```

Docker 的好处：

- 环境一致。
- 回滚方便。
- 适合 CI/CD。

当前项目可以先从 Jar 部署开始，稳定后再升级 Docker。

## 数据库部署

MySQL：

```text
开发：本机 MySQL
测试：测试服务器 MySQL / 云数据库测试库
生产：云数据库或独立 MySQL
```

Redis：

```text
开发：本机 Redis
测试：测试 Redis
生产：云 Redis 或独立 Redis
```

数据库上线前必须确认：

- Flyway 迁移成功。
- 生产库有备份。
- 账号权限最小化。
- 生产密码未写入 Git。

## Nginx 反向代理

示例：

```nginx
server {
    listen 443 ssl;
    server_name api.example.com;

    ssl_certificate     /etc/nginx/certs/api.example.com.crt;
    ssl_certificate_key /etc/nginx/certs/api.example.com.key;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

生产环境不要直接让用户访问：

```text
http://服务器IP:8080
```

应使用：

```text
https://api.example.com
```

## 移动端发布

测试 APK：

```text
VITE_API_BASE_URL=https://api-test.example.com
```

正式 APK：

```text
VITE_API_BASE_URL=https://api.example.com
```

修改后重新打包：

```bash
./scripts/build-mobile-apk.sh
```

发布 APK 前检查：

- App 能打开。
- 能登录。
- 库存查询正常。
- 入库/出库页面能访问。
- 后端域名是测试或生产正确域名。
- DCloud AppKey 没报错。

## 发布流程

```text
feature 分支开发
        ↓
合并 develop
        ↓
测试环境部署
        ↓
release 分支冻结
        ↓
发布检查
        ↓
合并 main
        ↓
打 tag
        ↓
生产部署
```

发布 tag 示例：

```bash
git tag v0.2.0
git push origin v0.2.0
```

## 回滚策略

后端回滚：

- 保留上一个 Jar 或 Docker 镜像。
- 如果新版本异常，切回旧版本并重启。

移动端回滚：

- 保留上一个 APK。
- 如果新版 APK 有问题，重新分发旧 APK。

数据库回滚：

- 数据库结构变更要谨慎。
- 发布前备份。
- Flyway migration 一旦生产执行，不要随意修改历史 migration。
- 大变更需要写回滚 SQL 或兼容双版本。

## 生产安全最低要求

- 后端必须 HTTPS。
- 生产 `JWT_SECRET` 必须更换。
- 数据库和 Redis 不直接暴露公网。
- 后台管理账号使用强密码。
- 服务器开放端口最小化。
- 生产日志不要输出密码、token、证书内容。
- 定期备份数据库。

