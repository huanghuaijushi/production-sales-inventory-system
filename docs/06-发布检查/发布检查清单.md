# 发布检查清单

本文档用于每次发布前检查。发布负责人可以复制一份到版本记录中，逐项勾选。

## 版本信息

```text
版本号：
发布日期：
发布分支：
发布人：
后端地址：
Web 地址：
移动端 APK：
数据库变更：
```

## Git 检查

- [ ] 当前代码来自 `release/*` 或准备合入 `main` 的稳定分支。
- [ ] 没有未提交的业务代码。
- [ ] 没有提交 `node_modules`、`dist`、`target`、APK、证书、缓存。
- [ ] commit 信息清楚。
- [ ] 发布后准备打 tag。

检查命令：

```bash
git status
git log --oneline -5
```

## 后端检查

- [ ] 后端主代码编译通过。
- [ ] 测试能跑的情况下测试通过。
- [ ] 应用能启动。
- [ ] Flyway migration 成功。
- [ ] 登录接口正常。
- [ ] 权限接口正常。
- [ ] 库存查询正常。
- [ ] 入库/出库核心接口正常。

常用命令：

```bash
cd production-sales-inventory-api
mvn -q -DskipTests compile
mvn -q -Dmaven.test.skip=true spring-boot:run
```

健康检查：

```bash
curl http://127.0.0.1:8080/api/v1/auth/status
```

## 数据库检查

- [ ] 数据库连接正常。
- [ ] 生产发布前已备份。
- [ ] migration 没有修改已经上线过的历史文件。
- [ ] 初始化数据不会重复污染生产数据。
- [ ] 账号权限符合环境要求。

检查命令：

```bash
mysql -h127.0.0.1 -upsi_user -ppsi_password production_sales_inventory -e "SELECT VERSION();"
```

## 前端 Web 检查

- [ ] 依赖安装正常。
- [ ] 类型检查或构建通过。
- [ ] 登录页面正常。
- [ ] 主要业务页面正常。
- [ ] API 地址指向正确环境。

## 移动端检查

- [ ] `.env.production` 指向正确后端地址。
- [ ] AppID 正确。
- [ ] Android 包名正确。
- [ ] DCloud AppKey 正确。
- [ ] 签名证书可用。
- [ ] APK 构建成功。
- [ ] 手机安装后不再提示 AppKey 错误。
- [ ] 手机能登录。
- [ ] 手机能查看库存。

打包命令：

```bash
./scripts/build-mobile-apk.sh
```

APK 输出目录：

```text
production-sales-inventory-android/simpleDemo/build/outputs/apk/release
```

## 内网穿透测试检查

如果使用 Cloudflare Tunnel：

- [ ] 后端本机 8080 正常。
- [ ] `cloudflared tunnel --url http://localhost:8080` 正在运行。
- [ ] 手机能打开 `/api/v1/auth/status`。
- [ ] APK 里打入的是当前 tunnel 地址。
- [ ] 已知该地址是临时地址，进程关闭后会失效。

## 生产发布检查

- [ ] 后端使用正式域名。
- [ ] HTTPS 正常。
- [ ] 数据库不是本地开发库。
- [ ] Redis 不是本地开发 Redis。
- [ ] JWT 密钥不是默认开发密钥。
- [ ] 云服务器安全组只开放必要端口。
- [ ] Nginx 配置已备份。
- [ ] 数据库已备份。

## 发布后验证

- [ ] 登录成功。
- [ ] 权限正常。
- [ ] 库存查询正常。
- [ ] 新增/修改/删除核心数据正常。
- [ ] 移动端 APK 访问正常。
- [ ] 后端日志没有持续异常。
- [ ] 数据库连接池没有异常。

## 回滚准备

- [ ] 保留上一版后端 Jar 或 Docker 镜像。
- [ ] 保留上一版 APK。
- [ ] 数据库有发布前备份。
- [ ] 明确回滚负责人。
- [ ] 明确回滚命令或操作步骤。

