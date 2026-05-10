# 移动端内网穿透说明

本文档说明如何用 Cloudflare Tunnel 把本机后端临时暴露成公网 HTTPS 地址，让手机不在同一个局域网时也能访问后端。

## 使用场景

本地开发时，后端通常运行在：

```text
http://localhost:8080
```

如果手机和电脑在同一个 Wi-Fi，可以用 Mac 局域网 IP 访问，例如：

```text
http://172.30.24.5:8080
```

如果手机不在同一个 Wi-Fi，或者要给外地同事测试，就需要内网穿透，把本机 8080 映射成公网 HTTPS 地址：

```text
https://xxxx.trycloudflare.com
```

## 安装 cloudflared

只需要安装一次：

```bash
brew install cloudflare/cloudflare/cloudflared
```

检查是否安装成功：

```bash
cloudflared --version
```

## 启动后端

先确保 Spring Boot 后端正在运行：

```bash
cd production-sales-inventory-api
mvn -q -Dmaven.test.skip=true spring-boot:run
```

如果提示 8080 已占用，说明可能已经有一个后端进程在跑。可以测试：

```bash
curl http://127.0.0.1:8080/api/v1/auth/status
```

正常返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "authenticated": false
  }
}
```

## 启动内网穿透

在项目根目录或任意目录运行：

```bash
cloudflared tunnel --url http://localhost:8080
```

启动成功后，终端会输出类似：

```text
Your quick Tunnel has been created!
https://cabin-sherman-hypothesis-establishment.trycloudflare.com
```

这个地址就是临时公网后端地址。

本次测试使用的地址：

```text
https://cabin-sherman-hypothesis-establishment.trycloudflare.com
```

## 手机测试

用手机浏览器打开：

```text
https://cabin-sherman-hypothesis-establishment.trycloudflare.com/api/v1/auth/status
```

如果能看到类似下面返回，就说明内网穿透成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "authenticated": false
  }
}
```

`authenticated: false` 是正常的，表示接口通了，只是当前没有登录 token。

## 移动端改公网地址

修改：

```text
production-sales-inventory-mobile/.env.production
```

把地址改成 tunnel 地址：

```text
VITE_API_BASE_URL=https://cabin-sherman-hypothesis-establishment.trycloudflare.com
```

然后重新打包 APK：

```bash
./scripts/build-mobile-apk.sh
```

打包成功后，APK 会生成在：

```text
production-sales-inventory-android/simpleDemo/build/outputs/apk/release
```

示例文件名：

```text
production-sales-inventory-mobile-cabin-sherman-hypothesis-establishment.trycloudflare.com.apk
```

## 重要注意事项

Cloudflare quick tunnel 是临时地址。

只要 `cloudflared tunnel --url http://localhost:8080` 这个进程还在运行，地址就可用。进程关闭、电脑重启、网络变化后，地址可能失效。

失效后需要重新运行：

```bash
cloudflared tunnel --url http://localhost:8080
```

然后拿新的 `https://xxxx.trycloudflare.com` 地址，重新修改：

```text
production-sales-inventory-mobile/.env.production
```

再重新打包 APK：

```bash
./scripts/build-mobile-apk.sh
```

## 常见问题

### 手机打不开 tunnel 地址

优先检查：

- 后端 8080 是否正在运行。
- `cloudflared` 终端窗口是否还开着。
- 手机网络是否能访问 Cloudflare。
- 地址是否复制完整，包括 `https://`。

### 本机 curl 访问失败，但手机能打开

如果 Mac 开了代理、Fake-IP 或网络工具，本机终端可能把 `trycloudflare.com` 解析成 `198.18.x.x`，导致 curl TLS 失败。

这时以手机浏览器测试为准。只要手机能打开：

```text
/api/v1/auth/status
```

就可以给 APK 使用。

### 登录或库存接口报 401

这是正常权限控制。

未登录时：

```text
/api/v1/auth/status
```

应该返回 `authenticated: false`。

库存接口需要登录 token。先在 App 登录，再进入库存查询。

## 临时测试与正式上线区别

内网穿透适合：

- 临时手机测试。
- 给同事短时间体验。
- 演示本机开发环境。

不适合：

- 长期生产使用。
- 多人稳定访问。
- 正式客户环境。

正式上线建议把后端部署到云服务器，并使用固定域名：

```text
https://api.your-domain.com
```

