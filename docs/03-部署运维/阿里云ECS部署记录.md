# 阿里云 ECS 部署记录

本文记录项目在阿里云 ECS 上的部署方式。文档中的服务器地址、仓库地址和本地密钥路径均使用占位符，实际值只在运维人员本地保存。

## 访问地址

```text
Web 访问：http://<SERVER_HOST>
健康检查：http://<SERVER_HOST>/actuator/health
服务器系统：Alibaba Cloud Linux 3
部署分支：main
部署目录：<APP_DIR>
前端目录：<WEB_ROOT>
```

当前部署结构：

```text
浏览器
  -> Nginx :80
  -> <WEB_ROOT> 前端静态文件
  -> /api/ 反向代理到 127.0.0.1:8080
  -> Spring Boot API
  -> MySQL / Redis
```

## 已安装环境

```text
Java 21
Maven 3.9.10：<MAVEN_BIN>
Node.js 20
npm 10
MySQL 8
Redis 6
Nginx 1.20
Git
```

## 服务器服务

后端 systemd 服务名：

```bash
systemctl status psi-api
systemctl restart psi-api
journalctl -u psi-api -f
```

依赖服务：

```bash
systemctl status mysqld
systemctl status redis
systemctl status nginx
```

后端环境变量文件：

```text
<APP_ENV_FILE>
```

该文件保存数据库密码和 JWT 密钥，只在服务器上维护，不提交到 Git。

## 数据库

```text
数据库名：production_sales_inventory
业务账号：<DB_USER>
迁移方式：Spring Boot 启动时自动执行 Flyway V1
```

生产库默认不导入演示数据。需要测试数据时，再手动导入：

```bash
mysql -u<DB_USER> -p <DB_NAME> < <APP_DIR>/production-sales-inventory-api/src/main/resources/db/seed/demo_data.sql
```

## 重新部署

在服务器执行：

```bash
cd <APP_DIR>
git fetch origin main
git checkout main
git reset --hard origin/main
```

构建后端：

```bash
cd <APP_DIR>/production-sales-inventory-api
JAVA_HOME=<JAVA_HOME> <MAVEN_BIN> -q -DskipTests package
systemctl restart psi-api
```

构建前端：

```bash
cd <APP_DIR>/production-sales-inventory-web
npm ci
npm run build
rm -rf <WEB_ROOT>/*
cp -a dist/. <WEB_ROOT>/
systemctl reload nginx
```

验证：

```bash
curl -I http://<SERVER_HOST>/
curl http://127.0.0.1:8080/actuator/health
```

## 安全组

阿里云安全组至少需要：

```text
22/tcp：SSH 登录，建议只允许自己的公网 IP
80/tcp：HTTP Web 访问
443/tcp：后续上 HTTPS 后开放
```

不要开放 MySQL 3306、Redis 6379、后端 8080 到公网。

## 后续建议

- 绑定域名后加 HTTPS。
- 把 SSH 的 22 端口来源限制为固定公网 IP。
- 配置 MySQL 定时备份。
- 发布正式版本时打 Git tag，例如 `v1.0.0`。
