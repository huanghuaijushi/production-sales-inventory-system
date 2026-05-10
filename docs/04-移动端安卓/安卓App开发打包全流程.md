# 安卓 App 开发打包全流程

本文档面向不熟悉 HBuilderX、DCloud、Android Studio、Android 证书和离线打包流程的开发者，说明本项目安卓 App 从开发到打包安装需要做什么、配置在哪里、需要获取哪些 ID，以及常用命令是什么。

## 你先记住三个目录

移动端源码项目：

```text
production-sales-inventory-mobile
```

以后写页面、改接口、改样式、改业务逻辑，主要都在这里。

安卓离线打包工程：

```text
production-sales-inventory-android
```

这是本机打 APK 的 Android 壳工程。一般不写业务代码，只用于打包。

文档目录：

```text
docs/04-移动端安卓
```

移动端相关说明都在这里。

## 整体流程

```text
1. 在 production-sales-inventory-mobile 开发 uniapp 页面
2. H5 模式调试页面和接口
3. 确认后端地址
4. 构建 App 资源
5. 同步 App 资源到 Android 离线工程
6. Gradle 打 APK
7. 手机安装测试
```

现在已经封装成一键脚本：

```bash
./scripts/build-mobile-apk.sh
```

## 一、HBuilderX 是干什么的

HBuilderX 是 DCloud 官方 IDE，用来开发和运行 uniapp 项目。

本项目可以用 HBuilderX 做这些事：

- 打开 `production-sales-inventory-mobile`
- H5 运行调试
- App 资源构建
- 云打包 APK

但是我们现在主要使用命令行和 Android 离线工程打 APK，HBuilderX 作为辅助工具即可。

## 二、Android Studio 是干什么的

Android Studio 是安卓原生开发工具。

本项目里它主要用于：

- 打开 `production-sales-inventory-android`
- 查看 Android 原生配置
- 本地 Gradle 打包
- 检查 SDK、build tools、签名配置
- 连接手机安装调试

你平时不需要在 Android Studio 里写 Vue 页面。

## 三、DCloud 开发者后台需要什么

DCloud 后台地址：

```text
https://dev.dcloud.net.cn/
```

需要配置或获取这些东西：

```text
AppID
Android 包名
Android 签名 SHA1
Android AppKey
```

### AppID

AppID 是 DCloud 给 uniapp 应用分配的 ID。

本地查看位置：

```text
production-sales-inventory-mobile/manifest.json
production-sales-inventory-mobile/src/manifest.json
```

注意：真实 AppID 可以保存在项目配置里，但文档里不要反复写敏感配置细节。

### Android 包名

Android 包名是 APK 的唯一应用标识。

本地查看位置：

```text
production-sales-inventory-android/simpleDemo/build.gradle
```

字段：

```gradle
applicationId "你的包名"
namespace "你的包名"
```

DCloud 后台里的 Android 包名必须和这里一致。

### 签名 SHA1 / SHA256

DCloud 后台生成 Android AppKey 时，需要证书签名信息。

查看命令：

```bash
keytool -list -v -keystore production-sales-inventory-mobile/certs/你的证书文件.keystore
```

命令会要求输入证书库密码，然后输出：

```text
SHA1:
SHA256:
```

注意：

- 证书文件不要提交 Git。
- 证书密码不要写进文档。
- SHA1、SHA256 也不要写进公共文档。

### Android AppKey

Android AppKey 是 DCloud 根据：

```text
AppID + Android 包名 + 签名信息
```

生成的应用校验 key。

配置位置：

```text
production-sales-inventory-android/simpleDemo/src/main/AndroidManifest.xml
```

字段示例：

```xml
<meta-data
    android:name="dcloud_appkey"
    android:value="${DCloud 后台生成的 Android AppKey}" />
```

如果 AppKey 不正确，手机启动 APK 会提示：

```text
未配置appkey或配置错误
```

## 四、证书是什么

Android 发布包需要签名证书。证书用于证明 APK 是同一个开发者发布的。

证书目录：

```text
production-sales-inventory-mobile/certs/
```

该目录已加入 `.gitignore`，不要提交。

生成证书示例：

```bash
keytool -genkeypair \
  -alias 你的证书别名 \
  -keyalg RSA \
  -keysize 2048 \
  -validity 36500 \
  -keystore production-sales-inventory-mobile/certs/你的证书文件.keystore
```

生成后要记录：

```text
证书文件位置
证书别名
证书库密码
证书私钥密码
SHA1
SHA256
```

这些信息应该保存在私密位置，不要写入 Git。

## 五、Android 离线打包工程怎么来的

DCloud 官方 Android 离线 SDK 下载地址：

```text
https://nativesupport.dcloud.net.cn/AppDocs/download/android
```

下载后通常会有：

```text
HBuilder-Integrate-AS
SDK
HBuilder-HelloUniApp
UniPlugin-Hello-AS
```

本项目使用 `HBuilder-Integrate-AS` 作为离线打包工程基础，复制到：

```text
production-sales-inventory-android
```

注意：这个目录体积大，包含 SDK、AAR、APK 构建产物，不提交 Git。

## 六、日常开发命令

进入移动端项目：

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

## 七、后端地址怎么配置

移动端生产环境配置：

```text
production-sales-inventory-mobile/.env.production
```

配置项：

```text
VITE_API_BASE_URL=你的后端地址
```

本机 H5 开发：

```text
VITE_API_BASE_URL=http://localhost:8080
```

局域网手机测试：

```text
VITE_API_BASE_URL=http://你的Mac局域网IP:8080
```

Cloudflare Tunnel 测试：

```text
VITE_API_BASE_URL=https://xxxx.trycloudflare.com
```

正式云部署：

```text
VITE_API_BASE_URL=https://api.example.com
```

注意：换地址后必须重新打 APK，已经安装在手机上的旧 APK 不会自动更新地址。

## 八、一键打 APK

在项目根目录运行：

```bash
./scripts/build-mobile-apk.sh
```

脚本会自动完成：

```text
1. npm run build:app
2. 同步 dist/build/app 到 Android 离线工程
3. ./gradlew :simpleDemo:assembleRelease
4. 复制出一个带后端地址标识的 APK 文件
```

APK 输出目录：

```text
production-sales-inventory-android/simpleDemo/build/outputs/apk/release
```

## 九、手动打包步骤

不推荐日常手动操作，但你要知道脚本背后做了什么。

构建 uniapp App 资源：

```bash
cd production-sales-inventory-mobile
npm run build:app
```

App 资源生成位置：

```text
production-sales-inventory-mobile/dist/build/app
```

同步到 Android 工程：

```bash
rm -rf production-sales-inventory-android/simpleDemo/src/main/assets/apps/你的AppID/www
mkdir -p production-sales-inventory-android/simpleDemo/src/main/assets/apps/你的AppID/www
cp -R production-sales-inventory-mobile/dist/build/app/. production-sales-inventory-android/simpleDemo/src/main/assets/apps/你的AppID/www/
```

Gradle 打包：

```bash
cd production-sales-inventory-android
./gradlew :simpleDemo:assembleRelease
```

## 十、Android Studio 怎么打开

打开 Android Studio 后：

```text
Open
  ↓
选择 production-sales-inventory-android
```

等待 Gradle Sync 完成。

常看位置：

```text
simpleDemo/build.gradle
simpleDemo/src/main/AndroidManifest.xml
simpleDemo/src/main/assets/apps/你的AppID/www
simpleDemo/build/outputs/apk/release
```

如果 Gradle Sync 报 SDK 缺失：

```text
Settings
  ↓
Languages & Frameworks
  ↓
Android SDK
```

安装需要的：

```text
Android SDK Platform
Android SDK Build-Tools
Android SDK Platform-Tools
```

## 十一、手机安装 APK

方式一：手动传输

```text
把 APK 发到微信/文件助手/数据线
手机点击安装
```

第一次安装可能需要开启：

```text
允许从未知来源安装
```

方式二：adb 安装

先确认 adb：

```bash
/Users/wenzhifei/Library/Android/sdk/platform-tools/adb devices
```

安装：

```bash
/Users/wenzhifei/Library/Android/sdk/platform-tools/adb install -r production-sales-inventory-android/simpleDemo/build/outputs/apk/release/你的APK.apk
```

如果签名不一致，先卸载旧 App：

```bash
/Users/wenzhifei/Library/Android/sdk/platform-tools/adb uninstall 你的包名
```

## 十二、常见错误

### 1. 未配置 appkey 或配置错误

原因：

- Android AppKey 没写。
- DCloud 后台包名和 APK 包名不一致。
- DCloud 后台签名 SHA1 和当前证书不一致。
- AppID 不一致。

处理：

```text
1. 检查 AppID
2. 检查 Android 包名
3. 用 keytool 查 SHA1
4. 到 DCloud 后台重新生成 AppKey
5. 写入 AndroidManifest.xml
6. 重新打 APK
```

### 2. 手机访问不了后端

原因：

- 后端没启动。
- 手机和电脑不在同一网络。
- `.env.production` 还是旧 IP。
- Cloudflare Tunnel 关闭了。
- APK 没重新打包。

检查：

```text
手机浏览器打开：
后端地址/api/v1/auth/status
```

### 3. 库存接口 401

这是正常权限控制。

先登录，再访问库存页面。

### 4. H5 根路径 404

需要确保移动端项目根目录有：

```text
index.html
```

并且包含：

```html
<div id="app"></div>
<script type="module" src="/src/main.ts"></script>
```

### 5. Gradle 报 SDK 缺失

打开 Android Studio 安装对应 SDK，或者检查：

```text
production-sales-inventory-android/local.properties
```

确保：

```text
sdk.dir=你的Android SDK路径
```

## 十三、哪些东西不要提交 Git

不要提交：

```text
production-sales-inventory-android/
production-sales-inventory-mobile/certs/
*.apk
node_modules/
dist/
.npm-cache/
.m2/
.env
*.local
证书密码
DCloud AppKey
签名 SHA1/SHA256
```

可以提交：

```text
production-sales-inventory-mobile 源码
scripts/build-mobile-apk.sh
docs/
.env.example
```

## 十四、正式上线时要改什么

临时测试时可以用：

```text
局域网 IP
trycloudflare.com
```

正式上线时要改成：

```text
https://api.example.com
```

正式上线前必须确认：

- 后端部署到云服务器。
- HTTPS 可用。
- `.env.production` 指向正式域名。
- DCloud AppKey 正确。
- 使用正式证书签名。
- APK 安装后能登录和查库存。

