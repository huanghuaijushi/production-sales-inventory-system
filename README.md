# 雯慧斋掌柜 · 产销存系统

雯慧斋掌柜产销存系统，采用前后端分离架构。

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
- 后端：Java 21、Spring Boot、Spring Security、JPA、Flyway
- 数据库：MySQL 8
- 缓存：Redis
- 部署：Nginx、Docker

## Business Scope

- 生产管理：BOM 配方、生产工单、领料、工序、成品入库
- 销售管理：订单导入、销售出库、渠道收益分析
- 采购管理：供应商、采购单、采购入库、原材料采购建议
- 库存管理：总账、批次库存、库存流水、盘点、调拨、报损
- 商品管理：成品 / 原材料分类、规格、售价、成本价、安全库存
- 经营看板：库存金额占比、分类占比、趋势图、毛利统计、预警

## Current Highlights

- 库存流水区分 `business_amount` 与 `cost_amount`
- 销售出库支持录入实际销售单价，自动计算毛利
- 生产成品入库按领料库存流水 `cost_amount` 汇总批次成本
- 成品 / 原料分类库存看板按库存金额统计，并展示 SKU 数与库存数量
- 产品管理分类侧栏仅显示启用分类

## Documentation

开发规范、业务边界、接口约定和部署规划见：

- [docs/development-guide.md](docs/development-guide.md)
- [docs/system-business-data-design.md](docs/system-business-data-design.md)
- [docs/system-improvement-plan.md](docs/system-improvement-plan.md)
- [docs/production-purchasing-design.md](docs/production-purchasing-design.md)
- [docs/production-purchasing-operation-guide.md](docs/production-purchasing-operation-guide.md)
- [docs/sales-order-import-center-design.md](docs/sales-order-import-center-design.md)

后端说明见：

- [production-sales-inventory-api/README.md](production-sales-inventory-api/README.md)
- [docs/database-setup.md](docs/database-setup.md)

前端说明见：

- [production-sales-inventory-web/README.md](production-sales-inventory-web/README.md)

## Local LAN Access

不上云、不部署到服务器时，可以把本机作为临时开发服务器给同一局域网的电脑或手机访问。

```bash
./scripts/start-lan.sh
```

脚本会自动检测本机局域网 IP，并同时启动：

- 后端：`0.0.0.0:8080`
- 前端：`0.0.0.0:5173`

启动后，同一 Wi-Fi 下的设备访问脚本输出的地址，例如：

```text
http://192.168.1.23:5173
```

如果 macOS 弹出网络访问提示，请允许 Java 和 Node.js 接收入站连接。停止服务按 `Ctrl+C`。
