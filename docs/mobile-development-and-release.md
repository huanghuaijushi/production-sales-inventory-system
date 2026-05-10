# 移动端开发与打包说明

本文档说明 `production-sales-inventory-mobile` 与 `production-sales-inventory-android` 的关系、日常开发流程、APK 打包流程和版本变更记录方式。

## 项目关系

`production-sales-inventory-mobile` 是移动端源码项目。

这里写 uniapp/Vue 代码，包括页面、接口、样式、状态管理、环境变量。以后新增功能、改页面、改接口地址，主要都在这个目录开发。

`production-sales-inventory-android` 是本地 Android 离线打包工程。

它不写业务页面，只负责把 `production-sales-inventory-mobile` 构建出来的 App 资源打成 APK。只有需要改包名、AppKey、签名证书、Android 权限、图标、原生 SDK 时，才需要动它。

整体流程：

```text
开发 production-sales-inventory-mobile
        ↓
npm run build:app
        ↓
同步 dist/build/app 到 production-sales-inventory-android
        ↓
Gradle 打 APK
```

## 日常开发

进入移动端目录：

```bash
cd production-sales-inventory-mobile
```

安装依赖：

```bash
npm install
```

H5 调试：

```bash
npm run dev:h5
```

浏览器访问：

```text
http://localhost:5173/#/
```

常用页面：

```text
首页：http://localhost:5173/#/pages/home/index
库存：http://localhost:5173/#/pages/inventory/index
登录：http://localhost:5173/#/pages/login/index
```

## 后端地址配置

移动端环境文件在：

```text
production-sales-inventory-mobile/.env.development
production-sales-inventory-mobile/.env.production
```

开发环境默认：

```text
VITE_API_BASE_URL=http://localhost:8080
```

APK 打包环境需要使用手机能访问到的 Mac 局域网 IP，例如：

```text
VITE_API_BASE_URL=http://172.30.24.5:8080
```

查看 Mac 当前局域网 IP：

```bash
ifconfig | sed -n '/inet /p'
```

手机测试后端是否可访问：

```text
http://你的Mac局域网IP:8080/api/v1/auth/status
```

如果手机访问不了，优先检查：

- 手机和 Mac 是否在同一个 Wi-Fi。
- 后端 Spring Boot 是否正在运行。
- Mac 防火墙是否拦截 8080。
- `.env.production` 是否还是旧 IP。

如果手机不在同一个局域网，可以使用 Cloudflare Tunnel 做临时内网穿透，详见：

```text
docs/mobile-tunnel-guide.md
```

## App 资源构建

构建 App 资源：

```bash
cd production-sales-inventory-mobile
npm run build:app
```

构建成功后，uniapp App 资源会生成在：

```text
production-sales-inventory-mobile/dist/build/app
```

这个目录不是最终 APK，它只是 Android 离线工程需要打包进去的前端资源。

## 本地 APK 打包

Android 离线工程目录：

```text
production-sales-inventory-android
```

推荐使用一键脚本打包：

```bash
./scripts/build-mobile-apk.sh
```

脚本会自动完成：

```text
1. 构建 production-sales-inventory-mobile
2. 同步 dist/build/app 到 Android 离线工程
3. 执行 Gradle release 打包
4. 复制生成带后端 IP 的 APK 文件
```

手动同步最新 App 资源：

```bash
rm -rf production-sales-inventory-android/simpleDemo/src/main/assets/apps/__UNI__B05BB5F/www
mkdir -p production-sales-inventory-android/simpleDemo/src/main/assets/apps/__UNI__B05BB5F/www
cp -R production-sales-inventory-mobile/dist/build/app/. production-sales-inventory-android/simpleDemo/src/main/assets/apps/__UNI__B05BB5F/www/
```

打 APK：

```bash
cd production-sales-inventory-android
./gradlew :simpleDemo:assembleRelease
```

APK 输出目录：

```text
production-sales-inventory-android/simpleDemo/build/outputs/apk/release
```

当前常用输出文件：

```text
production-sales-inventory-mobile-172.30.24.5.apk
```

安装到手机前，建议先卸载旧版本 APK，再安装新 APK。

## Android 配置信息

AppID、Android 包名、DCloud Android AppKey 属于应用发布配置。不要把真实值写入 Git 文档。

本地查看位置：

```text
production-sales-inventory-mobile/manifest.json
production-sales-inventory-mobile/src/manifest.json
```

Android 离线打包工程中的 AppKey 配置位置：

```text
production-sales-inventory-android/simpleDemo/src/main/AndroidManifest.xml
```

配置示例：

```xml
<meta-data
    android:name="dcloud_appkey"
    android:value="${DCloud 后台生成的 Android AppKey}" />
```

真实值只保存在：

```text
DCloud 开发者后台
本机 Android 离线打包工程
本机证书文件
```

注意：如果 AppKey、证书密码、签名指纹已经被提交到远端仓库历史，建议到 DCloud 后台重新生成 AppKey，并重新生成发布证书。

## 签名证书

证书文件只保存在本机，不提交 Git。

本机证书目录示例：

```text
production-sales-inventory-mobile/certs/
```

Git 已忽略该目录，不提交证书。

证书别名、证书库密码、证书私钥密码、SHA1、SHA256 不写入仓库文档。需要查看时在本机执行：

```bash
keytool -list -v -keystore production-sales-inventory-mobile/certs/你的证书文件.keystore
```

验证 APK 签名：

```bash
/Users/wenzhifei/Library/Android/sdk/build-tools/36.1.0/apksigner verify --verbose production-sales-inventory-android/simpleDemo/build/outputs/apk/release/simpleDemo-release.apk
```

看到下面结果说明签名可用：

```text
Verified using v1 scheme: true
Verified using v2 scheme: true
Number of signers: 1
```

## HBuilderX 打包说明

本项目已经可以通过本地 Android 离线工程打包 APK。HBuilderX 云打包也可以继续使用，但需要注意：

- 新应用不能使用公共测试证书打正式包。
- 离线打包必须配置 DCloud AppKey。
- 包名、AppID、签名 SHA1 必须与 DCloud 开发者后台一致。

如果 HBuilderX 弹出：

```text
未配置appkey或配置错误
```

说明 Android AppKey、包名或签名 SHA1 不匹配，需要到 DCloud 后台重新核对。

## 版本变更记录

建议以后每次发 APK 都记录：

```text
版本号：
日期：
后端地址：
APK 文件：
主要变更：
已知问题：
```

### v0.1.0 - 2026-05-10

后端地址：

```text
http://172.30.24.5:8080
```

APK：

```text
production-sales-inventory-android/simpleDemo/build/outputs/apk/release/production-sales-inventory-mobile-172.30.24.5.apk
```

主要变更：

- 新增 uniapp 移动端项目。
- 修复 H5 访问根路径 404。
- 重设计移动端首页，适配车间工人操作场景。
- 新增移动端库存查询页面。
- 对接后端库存接口，补齐 `/api/v1` 前缀。
- 区分开发环境和打包环境后端地址。
- 新增 Android 离线打包工程。
- 配置 Android 包名、DCloud AppKey、签名证书。
- 成功生成可安装 APK。

已知问题：

- `.env.production` 当前写的是 Mac 局域网 IP，换 Wi-Fi 或 IP 变化后需要重新修改并打包。
- Android 离线工程不提交 Git，仅作为本机打包工具箱保留。

## Git 提交建议

建议提交：

- `production-sales-inventory-mobile` 源码。
- 文档。
- 必要配置文件。

不要提交：

- `node_modules`
- `dist`
- `certs`
- APK 文件
- `production-sales-inventory-android`
- `.m2`
- `.npm-cache`
