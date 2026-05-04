# HHJS 产销存系统开发文档

## 1. 项目说明

本项目是一个前后端分离的 HHJS 产销存系统，面向生产、销售、采购、库存、供应商、客户、库存流水和经营统计等后台管理场景。

项目根目录名称为 `production-sales-inventory-system`，前端项目为 `production-sales-inventory-web`，后端项目为 `production-sales-inventory-api`。Java 基础包名为 `com.hhjs.psi`，业务产品名称统一使用“HHJS 产销存系统”。

## 2. 目录命名

```text
production-sales-inventory-system/
├── production-sales-inventory-web/
├── production-sales-inventory-api/
└── docs/
```

- `production-sales-inventory-web`：前端应用，负责页面、交互、状态管理、接口调用。
- `production-sales-inventory-api`：后端服务，负责业务接口、鉴权、数据库、缓存、事务处理。
- `docs`：项目文档目录，存放开发说明、接口说明、部署说明等文档。

## 3. 技术选型

### 3.1 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- 自定义 CSS
- 不使用 Element Plus、Ant Design Vue 等第三方 UI 组件库

前端可以按项目需要沉淀自己的基础组件，例如按钮、输入框、表格、弹窗、分页、表单项、状态标签等。

### 3.2 后端

- Java 21
- Spring Boot
- Spring Web
- Spring Validation
- Spring Security 或自定义 JWT 鉴权
- MyBatis Plus 或 Spring Data JPA
- MySQL 8
- Redis

### 3.3 部署

- Docker
- Docker Compose
- Nginx
- MySQL 8
- Redis

## 4. 核心功能范围

### 4.1 登录注册

- 管理员注册
- 管理员登录
- Token 鉴权
- 登录状态校验
- 退出登录

当前后端认证模块已实现以下接口：

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /api/v1/auth/me
GET  /api/v1/auth/status
POST /api/v1/auth/logout
```

认证实现约定：

- 密码使用 BCrypt 存储。
- 登录成功后签发 Bearer JWT。
- JWT 包含 `jti`、管理员 ID、用户名、角色列表、权限码列表、Token 版本和过期时间。
- 退出登录时将当前 JWT 的 `jti` 写入 Redis 黑名单，直到 Token 自然过期。
- 数据库结构通过 Flyway 迁移脚本管理。
- 管理员表为 `admin_user`。

### 4.1.1 RBAC 权限模型

认证模块采用 RBAC 模型，管理员、角色、权限之间通过关联表维护，方便后续扩展多管理员、多角色和细粒度接口权限。

当前核心表：

```text
admin_user
role
permission
admin_user_role
role_permission
```

设计约定：

- `admin_user`：管理员账号、密码摘要、状态、Token 版本等基础信息。
- `role`：角色定义，例如 `SUPER_ADMIN`、`ADMIN`。
- `permission`：权限码定义，例如 `product:view`、`stock:inbound`、`system:role:update`。
- `admin_user_role`：管理员和角色的多对多关系。
- `role_permission`：角色和权限的多对多关系。
- 第一个注册的管理员会绑定 `SUPER_ADMIN`，用于系统初始化。
- 系统初始化后，不建议继续开放公开管理员注册，后续管理员应通过用户管理模块创建并分配角色。

### 4.2 商品管理

商品基础字段建议包括：

- 商品编号
- 商品名称
- 分类
- 规格
- 单位
- 库存数量
- 最低库存
- 进货价
- 售价
- 备注

基础操作：

- 新增商品
- 修改商品
- 删除商品
- 查询商品
- 分页查询
- 条件筛选

### 4.3 采购管理

采购管理用于记录企业向供应商采购商品或原材料的业务过程。

字段建议包括：

- 采购单号
- 采购日期
- 供应商
- 商品
- 数量
- 采购单价
- 采购金额
- 经办人
- 单据状态
- 备注

基础操作：

- 新增采购单
- 修改采购单
- 删除采购单
- 查询采购单
- 采购入库

当前第一版手动采购闭环已实现：

```text
GET    /api/v1/suppliers
POST   /api/v1/suppliers
PUT    /api/v1/suppliers/{supplierId}
DELETE /api/v1/suppliers/{supplierId}

GET    /api/v1/purchase-orders
POST   /api/v1/purchase-orders
PUT    /api/v1/purchase-orders/{orderId}
POST   /api/v1/purchase-orders/{orderId}/inbound
POST   /api/v1/purchase-orders/{orderId}/cancel
```

第一版采购流程：

1. 维护供应商资料。
2. 手动创建采购单，选择供应商、商品、数量、采购单价和预计到货日期。
3. 采购单状态为 `草稿` 或 `待入库`。
4. 到货后点击确认入库，系统在同一事务中增加库存并写入采购入库流水。
5. 已入库采购单不能继续修改或取消。

前端页面：

```text
GET /purchase
```

页面包含采购单列表、供应商资料、库存采购建议和确认入库操作。

当前采购逻辑已经按粽子产销存场景优化为“原材料采购”：

- 采购单只能选择 `RAW_MATERIAL` 原材料。
- 成品粽子不允许直接采购，后续应通过生产单完成成品入库。
- 已新增成品配方、供货规则、生产建议和按供应商分组的智能采购建议。

相关文档：

```text
docs/production-purchasing-design.md
docs/production-purchasing-operation-guide.md
```

新增生产采购计划接口：

```text
GET    /api/v1/production/bom
POST   /api/v1/production/bom
DELETE /api/v1/production/bom/{bomItemId}

GET    /api/v1/production/supplier-materials
POST   /api/v1/production/supplier-materials
PUT    /api/v1/production/supplier-materials/{supplierMaterialId}
DELETE /api/v1/production/supplier-materials/{supplierMaterialId}

GET    /api/v1/production/capacity
GET    /api/v1/production/suggestions
GET    /api/v1/production/purchase-suggestions
```

### 4.4 销售管理

销售管理用于记录客户下单、销售出库和销售金额。

字段建议包括：

- 销售单号
- 销售日期
- 客户
- 商品
- 数量
- 销售单价
- 销售金额
- 经办人
- 单据状态
- 备注

基础操作：

- 新增销售单
- 修改销售单
- 删除销售单
- 查询销售单
- 销售出库

### 4.5 生产管理

生产管理用于记录生产任务、原料领用和成品入库。

字段建议包括：

- 生产单号
- 生产日期
- 成品
- 计划数量
- 完成数量
- 领料商品
- 领料数量
- 负责人
- 单据状态
- 备注

基础操作：

- 新增生产单
- 修改生产单
- 查询生产单
- 生产领料
- 成品入库

### 4.6 库存入库

记录采购入库、生产入库、盘盈等库存增加行为。

字段建议包括：

- 入库单号
- 入库时间
- 入库类型：采购入库、生产入库、盘盈、其他
- 商品
- 入库数量
- 关联业务单据
- 操作人
- 备注

### 4.7 库存出库

记录销售出库、生产领料、领用或损耗等库存减少行为。

字段建议包括：

- 出库单号
- 出库时间
- 出库类型：销售出库、生产领料、领用、损耗、其他
- 商品
- 出库数量
- 关联业务单据
- 操作人
- 备注

### 4.8 库存预警

当商品当前库存低于最低库存时，系统提示 `库存不足`。

建议支持：

- 首页统计库存不足数量
- 商品列表显示库存状态
- 库存预警列表

### 4.9 供应商管理

供应商字段建议包括：

- 供应商名称
- 联系人
- 电话
- 地址
- 备注

基础操作：

- 新增供应商
- 修改供应商
- 删除供应商
- 查询供应商

### 4.10 客户管理

客户字段建议包括：

- 客户名称
- 联系人
- 电话
- 地址
- 备注

基础操作：

- 新增客户
- 修改客户
- 删除客户
- 查询客户

### 4.11 库存流水

库存流水用于追踪每一次库存变化。

流水建议记录：

- 商品
- 操作类型：采购入库、销售出库、生产领料、生产入库、盘盈、盘亏、其他
- 操作数量
- 操作前库存
- 操作后库存
- 业务单据编号
- 操作人
- 操作时间
- 备注

### 4.12 数据统计

首页统计建议展示：

- 总商品数
- 库存不足数量
- 今日采购入库数量
- 今日销售出库数量
- 今日生产入库数量
- 供应商数量
- 客户数量
- 今日销售额
- 库存总价值

## 5. 推荐前端目录规划

后续初始化前端项目时，建议使用以下结构：

```text
production-sales-inventory-web/
├── public/
├── src/
│   ├── api/
│   ├── assets/
│   ├── components/
│   ├── layout/
│   ├── router/
│   ├── stores/
│   ├── styles/
│   ├── types/
│   ├── utils/
│   └── views/
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

目录说明：

- `api`：接口请求封装。
- `components`：项目自定义基础组件和业务组件。
- `layout`：后台布局，例如侧边栏、顶部栏、内容区。
- `router`：路由配置。
- `stores`：Pinia 状态管理。
- `styles`：全局样式、变量、布局样式。
- `types`：TypeScript 类型定义。
- `utils`：通用工具函数。
- `views`：页面级组件。

## 6. 推荐后端目录规划

后续初始化后端项目时，建议使用以下结构：

```text
production-sales-inventory-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hhjs/psi/
│   │   │       ├── common/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── mapper/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── vo/
│   │   └── resources/
│   └── test/
├── Dockerfile
└── pom.xml
```

说明：

- `controller`：RESTful API 控制器。
- `service`：业务逻辑。
- `mapper`：数据库访问层。
- `entity`：数据库实体。
- `dto`：请求参数对象。
- `vo`：接口返回视图对象。
- `common`：统一返回、分页对象、常量等。
- `exception`：全局异常处理。
- `security`：登录认证、权限校验、Token 处理。

`com/hhjs/psi` 是当前项目 Java 基础包名，其中 `hhjs` 表示你的命名空间，`psi` 表示产销存项目简称。

## 7. RESTful API 约定

接口统一前缀建议：

```text
/api/v1
```

资源命名使用复数名词：

```text
GET    /api/v1/products
POST   /api/v1/products
GET    /api/v1/products/{id}
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}
```

统一返回格式建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-05-03T10:00:00+08:00",
  "traceId": "request-trace-id"
}
```

分页参数建议：

```text
page=1
size=10
keyword=商品名称或编号
```

## 8. 数据库表规划

建议优先规划以下核心表：

- `admin_user`：管理员用户表。
- `product_category`：商品分类表。
- `product`：商品表。
- `supplier`：供应商表。
- `customer`：客户表。
- `purchase_order`：采购单表。
- `purchase_order_item`：采购单明细表。
- `sales_order`：销售单表。
- `sales_order_item`：销售单明细表。
- `production_order`：生产单表。
- `production_material_item`：生产领料明细表。
- `stock_inbound_order`：入库单表。
- `stock_outbound_order`：出库单表。
- `stock_ledger`：库存流水表。

库存数量应以后端事务为准，采购入库、销售出库、生产领料、生产入库和库存流水写入需要在同一个事务中完成。

本地数据库初始化说明见：

[database-setup.md](database-setup.md)

## 9. 缓存规划

Redis 建议用于：

- 登录 Token 或登录态缓存。
- 首页统计短期缓存。
- 字典数据或低频变更数据缓存。

库存扣减、采购入库、销售出库、生产领料、生产入库等核心写操作不建议只依赖缓存，必须以 MySQL 事务数据为准。

## 10. 开发规范

### 10.1 通用规范

- 文件夹使用小写短横线命名。
- 数据库表名使用小写下划线命名。
- REST API 使用复数资源名。
- 时间字段统一使用后端标准时间格式。
- 金额字段使用 `BigDecimal`，避免浮点误差。
- 删除操作建议优先使用逻辑删除。

### 10.2 前端规范

- Vue 单文件组件使用 PascalCase 命名。
- 业务页面放在 `views`。
- 通用组件放在 `components`。
- 接口类型统一放在 `types`。
- 页面不直接写复杂请求逻辑，应通过 `api` 层封装。
- CSS 按全局样式、布局样式、组件样式分层管理。

当前前端登录模块已实现：

```text
src/views/LoginView.vue
src/views/RegisterView.vue
src/views/DashboardView.vue
src/router/index.ts
src/stores/auth.ts
src/api/auth.ts
src/api/http.ts
src/utils/token-storage.ts
src/styles/main.css
```

登录实现约定：

- 使用 Vue 3 `<script setup lang="ts">`。
- 使用 Pinia 管理登录态。
- 使用 Vue Router 路由守卫保护后台页面。
- Token 根据“记住登录状态”存入 `localStorage` 或 `sessionStorage`。
- 注册页面调用 `POST /api/v1/auth/register`，注册成功后返回登录页。
- 页面样式全部使用项目自写 CSS。
- 不使用 Element Plus、Tailwind CSS 等第三方 UI/样式组件库。
- 本地开发后端 CORS 支持 `localhost`、`127.0.0.1` 和常见局域网地址访问前端开发服务器。

### 10.3 后端规范

- Controller 只处理参数校验和接口编排。
- Service 负责业务规则和事务。
- Mapper 或 Repository 负责数据库访问。
- DTO 用于请求入参。
- VO 用于接口返回。
- 统一异常处理，不在接口中直接返回堆栈信息。

## 11. 部署规划

推荐使用 Docker Compose 编排：

- `production-sales-inventory-web` 构建静态资源。
- Nginx 托管前端静态文件。
- Nginx 反向代理 `/api` 到后端服务。
- `production-sales-inventory-api` 作为 Spring Boot 服务运行。
- MySQL 8 存储业务数据。
- Redis 提供缓存能力。

建议部署结构：

```text
browser
  -> nginx
    -> web static files
    -> /api proxy to spring boot
      -> mysql
      -> redis
```

## 12. 后续开发顺序建议

1. 初始化前端 Vite 项目。
2. 初始化后端 Spring Boot 项目。
3. 编写数据库表结构。
4. 完成登录注册和鉴权。
5. 完成商品管理。
6. 完成供应商管理。
7. 完成客户管理。
8. 完成采购管理和采购入库。
9. 完成销售管理和销售出库。
10. 完成生产管理、生产领料和成品入库。
11. 完成库存流水、库存预警和首页统计。
12. 补充 Docker、Nginx、Docker Compose 部署配置。
