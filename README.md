# Production Sales Inventory System

HHJS 产销存系统项目，采用前后端分离架构。

## Project Structure

```text
production-sales-inventory-system/
├── production-sales-inventory-web/      # 前端项目：Vue 3 + TypeScript + Vite + 自定义 CSS
├── production-sales-inventory-api/      # 后端项目：Java 21 + Spring Boot
├── docker-compose.dev.yml               # 本地 MySQL + Redis 开发环境
└── docs/
    └── development-guide.md       # 项目开发文档
```

## Tech Stack

- 前端：Vue 3、TypeScript、Vite、自定义 CSS
- 后端：Java 21、Spring Boot、RESTful API
- 数据库：MySQL 8
- 缓存：Redis
- 部署：Nginx、Docker

## Business Scope

- 生产管理
- 销售管理
- 采购管理
- 库存管理
- 商品与供应商/客户资料管理
- 库存流水与经营统计

## Documentation

开发规范、功能边界、接口约定和部署规划见：

[docs/development-guide.md](docs/development-guide.md)

后端认证模块说明见：

[production-sales-inventory-api/README.md](production-sales-inventory-api/README.md)

前端登录模块说明见：

[production-sales-inventory-web/README.md](production-sales-inventory-web/README.md)

数据库初始化说明见：

[docs/database-setup.md](docs/database-setup.md)
