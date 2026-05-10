# 环境配置说明

本文档说明开发、测试、生产环境的配置拆分方式，避免把本地地址、临时地址、密码和生产配置写死在代码里。

## 环境划分

建议至少维护三套环境：

```text
dev    本地开发环境
test   测试环境
prod   生产环境
```

含义：

`dev`

开发人员本机使用。可以连接本地 MySQL、Redis、本地 Spring Boot。

`test`

给联调、验收、移动端测试使用。后端和数据库部署在测试服务器，数据可以重置。

`prod`

正式生产环境。只部署稳定版本，数据库需要备份和权限控制。

## 后端配置

后端配置文件：

```text
production-sales-inventory-api/src/main/resources/application.yml
```

后端已经通过环境变量读取关键配置，例如：

```text
SERVER_ADDRESS
SERVER_PORT
DB_URL
DB_USERNAME
DB_PASSWORD
DB_POOL_MAX_SIZE
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
JWT_SECRET
JWT_ACCESS_TOKEN_TTL
WEB_ORIGIN
```

本地开发示例：

```bash
export DB_URL='jdbc:mysql://localhost:3306/production_sales_inventory?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false'
export DB_USERNAME='psi_user'
export DB_PASSWORD='psi_password'
export REDIS_HOST='localhost'
export JWT_SECRET='change-this-dev-secret-for-production-sales-inventory-api-please'
```

生产环境注意：

- `JWT_SECRET` 必须换成高强度随机字符串。
- 数据库账号不要使用 root。
- 数据库密码不要写入 Git。
- Redis 如果暴露网络，必须配置密码和安全组。
- `WEB_ORIGIN` 配成真实前端域名。

## 移动端配置

移动端环境文件：

```text
production-sales-inventory-mobile/.env.development
production-sales-inventory-mobile/.env.production
```

本地 H5 开发：

```text
VITE_API_BASE_URL=http://localhost:8080
```

局域网 APK 测试：

```text
VITE_API_BASE_URL=http://你的Mac局域网IP:8080
```

内网穿透测试：

```text
VITE_API_BASE_URL=https://xxxx.trycloudflare.com
```

正式云部署：

```text
VITE_API_BASE_URL=https://api.example.com
```

注意：`trycloudflare.com` 是临时测试地址，不适合作为长期生产配置。

## 配置文件提交规则

可以提交：

```text
.env.example
.env.development
```

谨慎提交：

```text
.env.production
```

如果 `.env.production` 写的是临时 IP 或临时 tunnel 地址，建议只保留本地改动，不提交。

不要提交：

```text
.env
*.local
证书文件
生产密码
数据库备份
```

## 端口约定

默认端口：

```text
后端 API：8080
MySQL：3306
Redis：6379
移动端 H5：5173
```

如果端口冲突，优先通过环境变量改后端端口：

```bash
export SERVER_PORT=8081
```

## 环境检查命令

检查后端健康：

```bash
curl http://127.0.0.1:8080/api/v1/auth/status
```

检查 MySQL：

```bash
mysql -h127.0.0.1 -upsi_user -ppsi_password production_sales_inventory -e "SELECT VERSION();"
```

检查 Redis：

```bash
redis-cli ping
```

检查移动端生产地址是否打进构建产物：

```bash
strings production-sales-inventory-mobile/dist/build/app/app-service.js | sed -n '/http/p;/trycloudflare/p'
```

