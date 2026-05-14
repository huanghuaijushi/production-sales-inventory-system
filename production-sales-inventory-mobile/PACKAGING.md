# 移动端 APK 打包与实机验证指南

> 适用：production-sales-inventory-mobile → production-sales-inventory-android
> 阶段：Day 7

---

## 一、打包前自查（5 分钟）

### 1. 接口地址改成生产
`.env.production` 当前：

```
VITE_API_BASE_URL=http://localhost:8080
```

打包前**必须**改成可访问的服务端地址（公网域名 / 内网 IP）：

```
VITE_API_BASE_URL=https://your-domain.com
# 或
VITE_API_BASE_URL=http://192.168.x.x:8080
```

否则 APK 装到手机后请求 `localhost:8080` 会指向手机本机，没有后端。

### 2. 验证编译
```bash
cd production-sales-inventory-mobile
npm run type-check
npm run build:app -- --mode production
```

`build:app` 会产出 `dist/build/app/` 目录，里面是给原生壳子打包用的 H5 资源。

### 3. 检查 manifest
当前 `manifest.json` 已配置：

| 项 | 值 |
|---|---|
| `appid` | `__UNI__B05BB5F` |
| `versionName` | `0.1.0` |
| `versionCode` | `100` |
| Android `minSdkVersion` | 21（Android 5.0） |
| Android `targetSdkVersion` | 33（Android 13） |
| `abiFilters` | `armeabi-v7a` + `arm64-v8a` |
| 已申请权限 | INTERNET / NETWORK_STATE / WIFI_STATE / VIBRATE / WAKE_LOCK / EXT_STORAGE |
| 状态栏 | 沉浸式 + 暗色文字 + 白色背景 |
| splash | 渲染前显示 + 自动关闭 |

**未申请**（二期再加）：
- `CAMERA` — 现阶段无扫码功能
- `BLUETOOTH` — 现阶段无蓝牙扫码枪

---

## 二、打包方式（二选一）

### 方式 A：HBuilderX 云打包（推荐，最快）

1. 用 HBuilderX 打开 `production-sales-inventory-mobile/` 目录
2. 菜单：**发行 → 原生 App-云打包**
3. 勾选 Android，平台选项：
   - 自有证书 / DCloud 公共证书（测试用公共即可）
   - 包名：建议 `com.hhjs.psi.android`
   - 不勾选小程序
4. 提交后等 5-10 分钟，APK 在 `unpackage/release/apk/` 目录
5. 拖到手机安装

### 方式 B：本地离线打包（自定义需求时用）

需要：
- Android Studio
- App 离线 SDK：<https://nativesupport.dcloud.net.cn/AppDocs/usesdk/android>

按官方文档把 `dist/build/app/` 资源放进离线 SDK 工程，用 Gradle 打包。**第一次较折腾**，推荐先用方式 A。

---

## 三、APK 体积控制

uni-app App-plus 默认基座 ~25MB。如果实际超过 15MB，按需关闭 modules：

打开 `manifest.json` → `app-plus.modules`，**显式禁用**：

```jsonc
"modules": {
  "Audio": false,
  "VideoPlayer": false,
  "LivePusher": false,
  "LivePlayer": false,
  "Maps": false,
  "ChooseLocation": false,
  "iBeacon": false,
  "Bluetooth": false,
  "Barcode": false,
  "Fingerprint": false,
  "FaceID": false,
  "OAuth": false,
  "Payment": false,
  "Push": false,
  "Record": false,
  "Speech": false,
  "Share": false,
  "Statistic": false,
  "Webview-x5": false
}
```

> 这些 module 在 HBuilderX 打包页面也可以可视化勾选，效果一样。

预期裁剪后：~10-12 MB。

---

## 四、实机验证清单（按附录 B）

按下面顺序在 **至少 3 台不同品牌** Android 机型上测一遍：

### 功能层（必过）

- [ ] 安装 APK 后能正常启动，splash 显示不超过 1 秒
- [ ] 登录页能输入用户名密码，登录成功跳转工作台
- [ ] 工作台首页 4 个数字（今日入库/出库/低库存/缺货）正确显示
- [ ] 「需要关注」列表显示低库存 / 临期 / 原料预警
- [ ] 6 个常用按钮颜色和文字正确
- [ ] **入库**：选商品 → 填数量（弹纯数字键盘）→ 选日期 → 提交成功
- [ ] **出库**：选商品 → 批次默认选最临期 → 填数量 → 提交成功
- [ ] **盘点**：选商品 → 选批次 → 输实盘 → 差异颜色实时变 → 二次确认弹窗 → 提交成功
- [ ] **报损**：选商品 → 选批次 → 选损耗类型 → 数量 → 提交成功
- [ ] **查批次**：列表显示临期徽章（红/橙/琥珀/绿/灰）
- [ ] **查流水**：时间线显示，类型徽章颜色对，正负数字颜色对
- [ ] 「更换」按钮可重新选择商品
- [ ] 退出登录 → 回到登录页

### 工程层（影响体验）

- [ ] APK 文件大小 < 15 MB（用 `ls -lh` 看）
- [ ] 首页接口 payload < 20 KB（DevTools Network 看 `/mobile/home`）
- [ ] 状态栏不被内容覆盖（顶部信号 / 时间清晰可见）
- [ ] 底部 home indicator（小白条机型）不挡住「确认」按钮
- [ ] 盘点页进入后屏幕不自动锁屏（>10 秒不变暗）
- [ ] 退出盘点页屏幕恢复正常自动锁屏
- [ ] 数量输入框弹**纯数字键盘**（无小数点、无负号、无字母）
- [ ] 网络断开时操作有清晰错误提示（"网络请求失败" 或后端给的错误），**不自动登出**
- [ ] 网络恢复后下拉刷新或重新进入页面能继续用

### 数据层（流水回查）

操作完一轮后，去后端数据库验证：

- [ ] 入库 1 次 → `stock_record` 表多 1 条 `type=IN, sub_type=PURCHASE 或 PRODUCTION`
- [ ] 出库 1 次 → 多 1 条 `type=OUT, sub_type=SALES 或 PRODUCTION_USAGE`
- [ ] 报损 1 次 → 多 1 条 `type=OUT, sub_type=PACKAGING_LOSS / DAMAGE_LOSS / ...`
- [ ] 盘点 1 次 → `stock_check_order` 多 1 条 + `stock_check_order_item` 多 1 条 + 如果有差异 `stock_record` 多 1 条 `INVENTORY_GAIN/LOSS`
- [ ] 所有动作 `stock` 表数量变化正确，`stock_batch.available_quantity` 同步

---

## 五、推荐测试机型

| 优先级 | 品牌型号举例 | 关注什么 |
|---|---|---|
| **P0** | 你的实际客户用的型号 | 这是真实场景，必须通过 |
| **P1** | 华为 / 荣耀（HarmonyOS） | 国产壳子兼容性差异 |
| **P1** | 小米 / Redmi（MIUI） | 后台保活策略 / 权限弹窗 |
| **P2** | OPPO / vivo（ColorOS / Origin OS） | 状态栏 / 安全区 |
| **P3** | 一台旧设备（Android 8 / 9） | minSdk 21 兼容性 |

---

## 六、已知风险

- **Vue 3.5 + uni-app alpha 兼容性警告**：登录按钮点击时会有 Vue warn `Cannot assign to read only property '_'`，**不影响功能**。如果实机也复现，可考虑：
  - 降级 `vue` 到 `3.4.x` 并同步调整 overrides
  - 或升级 `@dcloudio/uni-app` 到匹配 3.5 的最新 alpha
  - 二选一是稳定性 trade-off，**等真实问题出现再处理**，不要为消除警告破坏现有工作流

- **uni.vibrateShort 在 H5 不可用**：代码用了可选链 `?.` 兜底，APK 端正常震动

- **Vite proxy 不带到 APK**：APK 直连 `VITE_API_BASE_URL`，**没有 proxy**，确保后端 CORS 允许 APK 包名访问（或简单 `Access-Control-Allow-Origin: *`）

- **离线模式**：当前没做离线队列。仓库角落断网时操作会报错失败。要求用户**回到信号好的地方**再补提交。如果客户实际抱怨，再排离线方案（计划文档 §11 二期）

---

## 七、Day 7 完成判据

下列三项**全部成立**才算阶段一上线就绪：

1. 代码侧：本文档第一节 + 第二节配置已落实
2. APK 产物：能装、能登录、能完成 §四 全部功能项
3. 客户实机：至少 1 台客户实际机型走完一遍，反馈"够用"

如果验收某项失败，回到对应 Day 的修复项重做，不要凑数上线。
