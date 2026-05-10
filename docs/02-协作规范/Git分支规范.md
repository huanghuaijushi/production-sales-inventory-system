# Git 分支规范

本文档定义项目的 Git 分支、提交、合并和发布 tag 规则。

## 分支模型

推荐使用这组分支：

```text
main        生产稳定分支，只放已上线或准备上线的代码
develop     日常集成分支，功能开发完成后先合到这里
feature/*   功能分支
fix/*       普通问题修复分支
release/*   发布准备分支
hotfix/*    线上紧急修复分支
```

## 分支职责

`main`

生产稳定分支。不要直接在 `main` 上开发业务功能。只有通过测试并准备发布的代码，才合入 `main`。

`develop`

日常集成分支。多个功能完成后先合入 `develop`，统一联调和测试。

`feature/*`

新功能分支，从 `develop` 拉出。例如：

```text
feature/mobile-inventory
feature/sales-order-import
feature/production-report
```

`fix/*`

非紧急问题修复分支，从 `develop` 拉出。例如：

```text
fix/mobile-login-error
fix/inventory-query-status
```

`release/*`

发布准备分支，从 `develop` 拉出。例如：

```text
release/v0.2.0
```

该分支只做发布前修复、版本号、文档、配置检查，不再新增大功能。

`hotfix/*`

线上紧急修复分支，从 `main` 拉出。例如：

```text
hotfix/prod-login-401
```

修复后合回 `main`，再同步回 `develop`。

## 日常开发流程

```text
git checkout develop
git pull
git checkout -b feature/your-feature
开发与自测
git commit
合并到 develop
测试环境验证
```

功能分支合并前要确认：

- 后端能编译。
- 前端能构建。
- 核心页面能打开。
- 接口没有明显 401/500 异常。
- 没有提交证书、APK、缓存、`node_modules`、`dist`。

## 发布流程

```text
develop
  ↓
release/v版本号
  ↓
测试环境验证
  ↓
main
  ↓
tag
  ↓
生产部署
```

示例：

```bash
git checkout develop
git pull
git checkout -b release/v0.2.0
```

测试通过后：

```bash
git checkout main
git merge release/v0.2.0
git tag v0.2.0
git push origin main
git push origin v0.2.0
```

再同步回 `develop`：

```bash
git checkout develop
git merge main
git push origin develop
```

## 提交信息建议

使用简单的约定式提交：

```text
feat: 新功能
fix: 修复问题
docs: 文档
chore: 构建、脚本、杂项
refactor: 重构
test: 测试
```

示例：

```text
feat: add mobile inventory query
fix: resolve permission repository lookup
docs: add mobile tunnel guide
chore: add mobile apk build script
```

## 不要提交的内容

这些内容不要进 Git：

```text
node_modules/
dist/
target/
.m2/
.npm-cache/
certs/
*.apk
production-sales-inventory-android/
.env
*.local
```

其中 `production-sales-inventory-android` 是本机 Android 离线打包工程，体积大且包含本地 SDK/AAR/APK，不作为源码提交。

## 当前建议

当前项目已经有 `main`。建议下一步创建 `develop`：

```bash
git checkout -b develop
git push origin develop
```

之后新功能都从 `develop` 拉分支，不直接在 `main` 上继续开发。

