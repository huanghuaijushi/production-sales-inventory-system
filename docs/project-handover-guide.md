# HHJS 产销存系统交接指南

> 适用对象：刚接手这个项目的实习生 / 新同学
>
> 目标：帮助你在最短时间内理解项目结构、业务流程、数据口径、开发方式、常见坑点和后续维护方向。

---

## 1. 这是什么项目

这是一个 **前后端分离的 HHJS 产销存系统**，主要服务于食品生产、原材料采购、成品销售、库存管理、批次追踪和经营分析。

你可以把它理解成一个“适合粽子工厂场景的小型 ERP / 产销存系统”。

### 核心业务范围

- 商品管理
- 原材料管理
- 供应商管理
- 采购管理
- 生产管理
- 销售管理
- 库存管理
- 批次库存
- 库存流水
- 经营看板
- 生产成本归集
- 销售收益分析

---

## 2. 项目目录结构

项目根目录通常长这样：

```text
production-sales-inventory-system/
├── production-sales-inventory-web/      # 前端：Vue 3 + TypeScript + Vite
├── production-sales-inventory-api/      # 后端：Spring Boot + JPA + Flyway
├── docs/                                # 项目文档
├── docker-compose.dev.yml               # 本地 MySQL / Redis
└── scripts/                             # 启动脚本
```

### 2.1 前端

`production-sales-inventory-web`

职责：

- 页面展示
- 用户交互
- 表格 / 弹窗 / 表单
- 调接口
- 管理页面状态

### 2.2 后端

`production-sales-inventory-api`

职责：

- REST API
- 鉴权
- 业务规则
- 数据库事务
- 成本计算
- 库存扣减 / 入库 / 批次管理

### 2.3 文档

`docs`

里面是这套系统的设计说明和操作说明。当前比较重要的文档有：

- `docs/development-guide.md`：整体开发规范与业务边界
- `docs/database-setup.md`：数据库初始化和种子数据说明
- `docs/system-business-data-design.md`：业务数据结构和口径
- `docs/system-improvement-plan.md`：系统后续优化计划
- `docs/production-purchasing-design.md`：生产采购计划设计
- `docs/production-purchasing-operation-guide.md`：生产采购操作指南
- `docs/sales-order-import-center-design.md`：销售导入中心设计

---

## 3. 技术栈

### 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- 自定义 CSS
- 不使用 Element Plus / Ant Design Vue 这类 UI 库

### 后端

- Java 21
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway
- MySQL 8
- Redis

### 运行环境

- 本地开发用 Docker Compose 起 MySQL / Redis
- 前后端本地开发模式运行
- 可选 LAN 模式给同局域网设备访问

---

## 4. 业务主线怎么理解

这个项目最重要的是理解**库存、生产、销售、采购之间的关系**。

### 4.1 采购链路

采购主要买的是 **原材料**，不是成品。

典型流程：

```text
维护供应商
-> 维护供货规则
-> 查看智能采购建议
-> 创建采购单
-> 采购到货确认入库
-> 原材料库存增加
-> 生成库存流水
```

采购后原材料会进入库存系统，库存批次会被记录。

### 4.2 生产链路

生产的目标是把原材料变成成品。

典型流程：

```text
配置 BOM 配方
-> 创建生产工单
-> 生成用料计划
-> 按批次领料
-> 工序流转 / 报工
-> 记录损耗
-> 成品入库
-> 成品批次生成 unit_cost
```

这里最关键的一点是：

> 成品批次成本不是拍脑袋写死的，而是由生产领料的库存流水成本汇总得来的。

### 4.3 销售链路

销售的目标是把成品卖出去。

典型流程：

```text
创建销售单
-> 锁定库存
-> 选择出库批次
-> 销售出库
-> 生成库存流水
-> 记录业务金额和成本金额
-> 计算毛利
```

现在销售出库时支持：

- 选择实际出库批次
- 录入实际销售单价
- 自动算收入 / 成本 / 毛利

### 4.4 库存链路

库存分为三层：

1. **商品总库存 `stock`**
   - 当前数量
   - 锁定数量
   - 可用数量

2. **批次库存 `stock_batch`**
   - 批次号
   - 批次数量
   - 批次可用数量
   - 批次单位成本 `unit_cost`

3. **库存流水 `stock_record`**
   - 每次入库 / 出库 / 调整都会记流水
   - 流水里区分
     - `business_amount`：收入/业务金额
     - `cost_amount`：成本金额

---

## 5. 当前最重要的金额口径

这个项目现在最容易混淆的点是金额字段，所以你一定要记住下面几条。

### 5.1 `business_amount`

表示业务收入金额。

常用于：

- 销售出库
- 销售收益统计
- 毛利分析

### 5.2 `cost_amount`

表示成本金额。

常用于：

- 采购入库成本
- 生产领料成本
- 成品批次成本
- 报损 / 盘亏成本

### 5.3 `stock_batch.unit_cost`

表示某个批次的单位成本。

来源规则：

- 采购批次：通常来自采购成本
- 生产批次：来自该生产工单领料流水 `cost_amount` 汇总后按成品入库数量分摊

### 5.4 首页分类库存占比

首页两个看板：

- 原料分类库存占比
- 成品分类库存占比

当前按 **库存金额** 统计，不是按库存数量统计。

并且每个分类卡片展示：

- 占比
- 库存金额
- SKU 数
- 当前库存数量

---

## 6. 页面怎么对应到业务

### 6.1 `DashboardView`

首页看板，主要看：

- 库存总览
- 原料分类库存占比
- 成品分类库存占比
- 热门商品
- 经营趋势
- 毛利统计

### 6.2 `InventoryView`

库存总览页面，主要看：

- 库存列表
- 库存流水
- 新增入库 / 出库
- 批次明细
- 库存金额、成本、毛利/损失

### 6.3 `ProductManagementView`

商品管理页面，主要看：

- 商品基础资料
- 分类管理
- 成品 / 原材料
- 成本价 / 售价
- 安全库存

### 6.4 `ProductionPlanView`

生产计划页面，主要看：

- 工单
- 配方用料计划
- 原料领料
- 工序损耗
- 成品入库

### 6.5 `PurchaseManagementView`

采购页面，主要看：

- 采购单
- 供应商
- 供货规则
- 智能采购建议
- 生产建议

### 6.6 `SalesManagementView`

销售页面，主要看：

- 销售单
- 渠道
- 销售导入
- 销售出库
- 锁库存 / 批次扣减

---

## 7. 你接手后最先要搞懂的几个后端类

### 7.1 `InventoryService`

位置：

```text
production-sales-inventory-api/src/main/java/com/hhjs/psi/inventory/service/InventoryService.java
```

职责：

- 入库
- 出库
- 库存调整
- 批次创建
- 库存流水生成
- 仪表盘统计

这是库存系统的核心。

### 7.2 `ProductionPlanningService`

位置：

```text
production-sales-inventory-api/src/main/java/com/hhjs/psi/production/service/ProductionPlanningService.java
```

职责：

- 生产工单
- BOM 配方
- 原料领料
- 工序流转
- 成品入库
- 成品批次成本计算

### 7.3 `PurchaseOrderService`

位置：

```text
production-sales-inventory-api/src/main/java/com/hhjs/psi/purchase/service/PurchaseOrderService.java
```

职责：

- 采购单创建 / 编辑 / 取消
- 采购入库
- 采购单状态流转

### 7.4 `StockRecord`

位置：

```text
production-sales-inventory-api/src/main/java/com/hhjs/psi/inventory/entity/StockRecord.java
```

这是库存流水实体，记录每次库存变动。

### 7.5 `StockBatch`

位置：

```text
production-sales-inventory-api/src/main/java/com/hhjs/psi/inventory/entity/StockBatch.java
```

这是批次库存实体，记录批次号、数量、可用数量和单位成本。

---

## 8. 你接手后最先要搞懂的几个前端页面

### 8.1 `DashboardView.vue`

首页看板页面。

重点看：

- 原料 / 成品分类库存占比
- 看板卡片
- 趋势图
- 毛利图表

### 8.2 `InventoryView.vue`

库存管理页面。

重点看：

- 库存列表
- 库存流水
- 出入库弹窗
- 批次选择
- 金额预估

### 8.3 `StockOperationModal.vue`

库存操作弹窗。

重点看：

- 入库 / 出库切换
- 销售单价输入
- 批次选择
- 批次成本预估
- 毛利预估

### 8.4 `ProductManagementView.vue`

商品管理页面。

重点看：

- 商品分类树
- 搜索分类
- 商品启用状态
- 分类管理

### 8.5 `ProductionPlanView.vue`

生产计划页面。

重点看：

- 工单生命周期
- 领料
- 工序损耗
- 成品入库

---

## 9. 常见操作和注意事项

### 9.1 为什么登录后会 401

常见原因：

- token 失效
- 数据库重建后用户变了
- 浏览器里存了旧 token
- 后端重启后登录状态需要重新获取

### 9.2 为什么批次下拉可能点不了

常见原因：

- 还没选择商品
- 该商品没有批次
- 批次接口正在加载
- 当前页面不是出库模式

### 9.3 为什么成品批次成本可能不准

常见原因：

- 看的是旧批次
- 老数据在新逻辑上线前已经生成
- 生产工单没有走完整领料 / 入库流程

### 9.4 为什么库存金额和数量不是一回事

因为现在首页按的是 **库存金额**，不是库存件数。

库存金额是：

```text
库存数量 × 单位成本
```

---

## 10. 本地开发怎么跑

### 10.1 启动数据库

如果项目根目录有 Docker Compose：

```bash
docker compose -f docker-compose.dev.yml up -d
```

### 10.2 启动后端

```bash
cd production-sales-inventory-api
mvn spring-boot:run
```

### 10.3 启动前端

```bash
cd production-sales-inventory-web
npm install
npm run dev
```

### 10.4 看日志和问题

优先看：

- 后端控制台日志
- 浏览器控制台 Network 请求
- `401 / 403 / 500` 返回内容

---

## 11. 你接手后建议按这个顺序熟悉

### 第一步
先看首页和库存页面，理解库存金额与分类看板。

### 第二步
看商品管理、供应商管理、采购建议，理解“原材料如何进入库存”。

### 第三步
看生产计划，理解“原材料如何变成成品批次成本”。

### 第四步
看销售管理，理解“成品如何按批次出库并形成收益”。

### 第五步
看文档里的业务口径，确认你改代码时不要把：

- 数量
- 库存金额
- 成本金额
- 业务金额
- 批次单位成本

混成一锅。

---

## 12. 建议你优先记住的结论

这套系统最关键的主线是：

```text
采购原材料
-> 原材料入库
-> 生产领料
-> 生产成本归集
-> 成品入库
-> 销售出库
-> 收入/成本/毛利统计
```

如果你把这条线看懂，整个项目就懂了一大半。

---

## 13. 你改代码时最容易踩的坑

1. **只改前端不改后端字段**
   - 例如前端加了 `businessUnitPrice`，后端 DTO 没加，会导致启动或接口对不上。

2. **只改统计不改数据来源**
   - 例如首页想显示库存金额，但流水和批次没有正确记录成本。

3. **把数量和金额混用**
   - 数量是件数，金额是钱，不能互相替代。

4. **只看旧数据判断新逻辑**
   - 很多成本逻辑只对新流程生效，老数据不会自动回填。

5. **忘记刷新前端缓存**
   - 改完代码后页面还是旧样式，往往是浏览器缓存或热更新没生效。

---

## 14. 如果你要继续做开发

建议你先熟悉这几个优先级最高的任务：

- 让生产工单闭环更完整
- 让库存批次成本更准确
- 让销售导入中心更可用
- 让首页看板和报表继续对齐真实口径
- 让权限和用户管理更稳定

---

## 15. 一句话总结

这个项目不是简单的库存管理，而是一个 **围绕生产、采购、销售、库存、批次和成本归集的轻量产销存系统**。

你先记住一句话：

> 业务流走通，库存才准；库存批次准，成本才准；成本准，毛利才准。
