# 雯慧斋掌柜 · 产销存系统 · Web

Frontend application for 雯慧斋掌柜 production, sales and inventory system.

## Stack

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Custom CSS

No third-party component library is used.

## Run

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173`.

## Environment

Create `.env.local` when needed:

```bash
VITE_API_BASE_URL=/api/v1
```

During development, Vite proxies `/api` to `http://localhost:8080`.

## Main Pages

- `/login`：管理员登录。
- `/register`：管理员注册。
- `/dashboard`：经营看板，包含库存概览、趋势图、原料 / 成品分类库存占比、利润看板。
- `/inventory`：库存总览、库存流水、入库 / 出库操作、批次信息。
- `/products`：商品、分类、规格、成本价、售价管理。
- `/production`：生产工单、领料、工序、成品入库、生产建议。
- `/purchase`：采购单、供应商、供货规则、采购建议。
- `/sales`：销售单、导入中心、渠道收益分析。

## Current Notes

- 库存出库会展示实际销售单价、成本单价、预估收入、预估成本和毛利预估。
- 销售出库批次可手动选择。
- 原料 / 成品分类看板按库存金额展示，并附 SKU 数和库存数量。
- 分类管理侧栏只显示启用分类。

Registration calls `POST /api/v1/auth/register`. After successful registration, the app redirects back to `/login`.
