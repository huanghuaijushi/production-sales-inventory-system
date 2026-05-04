# 生产采购计划设计文档

## 1. 设计目标

本模块把采购从“看到库存低就买”升级为“先看成品要生产多少，再倒推出原材料要买多少”。

当前适用于粽子产销存场景：

- 粽子是成品，通过生产补货。
- 糯米、豆沙馅、猪肉、粽叶、咸蛋黄等是原材料，通过采购补货。
- 每个成品可以配置多条原材料配方。
- 每个原材料可以绑定默认供应商、单价、起订量、采购倍数和交期。
- 系统根据成品库存预警自动计算生产建议和采购建议。

## 2. 核心概念

### 2.1 BOM 配方

BOM 表示生产一个成品需要消耗哪些原材料。

示例：

```text
豆沙粽 1 个 = 糯米 0.0200 袋 + 豆沙馅 0.0100 袋 + 粽叶 0.0040 包
```

字段：

- 成品
- 原材料
- 单位用量
- 损耗率

损耗率用于覆盖生产损耗，例如 `0.0300` 表示 3%。

### 2.2 供货规则

供货规则表示某个供应商可以供应某个原材料，以及采购约束。

字段：

- 供应商
- 原材料
- 默认单价
- 最小起订量
- 采购倍数
- 交期天数
- 是否优先

示例：

```text
糯米供应商 -> 糯米 -> 单价 120 -> 起订 1 袋 -> 倍数 1 -> 交期 3 天
```

## 3. 数据模型

新增表：

```text
bom_item
supplier_material
```

### 3.1 bom_item

```text
id
finished_product_id
material_product_id
quantity_per_unit
loss_rate
created_at
updated_at
```

约束：

- `finished_product_id + material_product_id` 唯一。
- 成品必须是 `FINISHED_PRODUCT`。
- 原材料必须是 `RAW_MATERIAL`。
- 单位用量必须大于 0。
- 损耗率范围为 0 到 1。

### 3.2 supplier_material

```text
id
supplier_id
product_id
default_unit_price
min_order_quantity
order_multiple
lead_time_days
preferred
remark
created_at
updated_at
```

约束：

- `supplier_id + product_id` 唯一。
- 商品必须是 `RAW_MATERIAL`。
- 起订量和采购倍数必须大于 0。
- 交期不能小于 0。

## 4. 计算规则

### 4.1 可生产量

每个原材料先计算它能支持生产多少成品：

```text
单个原材料可支持数量 = floor(原材料可用库存 / (单位用量 * (1 + 损耗率)))
```

成品最大可生产量取所有原材料中的最小值：

```text
成品最大可生产量 = min(所有原材料可支持数量)
```

最小值对应的原材料就是当前瓶颈原材料。

### 4.2 生产建议

当前第一版采用保守策略，目标是不堆货：

```text
建议生产量 = max(库存预警线 - 当前成品库存, 0)
```

只有成品库存低于预警线时才生成生产建议。

### 4.3 原材料需求

```text
原材料需求量 = ceil(建议生产量 * 单位用量 * (1 + 损耗率))
```

### 4.4 原材料缺口

```text
原材料缺口 = max(原材料需求量 - 当前可用库存 - 待入库采购数量, 0)
```

系统会把 `PENDING_INBOUND` 状态的采购单计入待入库数量，避免重复采购。

### 4.5 建议采购量

如果缺口大于 0，按供货规则修正：

```text
建议采购量 = max(缺口, 最小起订量)
建议采购量 = 向上取整到采购倍数
```

示例：

```text
缺口 17，起订 10，采购倍数 5 -> 建议采购 20
缺口 6，起订 10，采购倍数 5 -> 建议采购 10
```

## 5. 后端接口

接口前缀：

```text
/api/v1/production
```

当前已实现：

```text
GET    /production/bom
POST   /production/bom
PUT    /production/bom/{bomItemId}
DELETE /production/bom/{bomItemId}

GET    /production/supplier-materials
POST   /production/supplier-materials
PUT    /production/supplier-materials/{supplierMaterialId}
DELETE /production/supplier-materials/{supplierMaterialId}

GET    /production/capacity
GET    /production/suggestions
GET    /production/purchase-suggestions
```

## 6. 前端页面

页面路径：

```text
/purchase
```

当前页面包含：

- 采购单列表
- 手动新增采购单
- 采购单确认入库
- 智能采购建议
- 生产建议
- 供应商资料
- 成品配方维护
- 原材料供货规则维护

## 7. 当前边界

当前版本属于“轻量 MRP 第一版”，已经能解决手工采购量不严谨的问题，但还不是完整 ERP。

当前未做：

- 销售预测。
- 真实销售订单锁库存。
- 生产单状态流转。
- 自动领料。
- 成品自动入库。
- 多供应商比价。
- 自动通知供应商。
- 批次先进先出或临期规则。

## 8. 后续优化路线

### 第一阶段：手动计划

已完成：

- BOM 配方。
- 供货规则。
- 可生产量。
- 生产建议。
- 按供应商分组的采购建议。
- 手动生成采购单。

### 第二阶段：生产单闭环

建议新增：

- 生产计划单。
- 生产领料。
- 成品入库。
- 生产损耗记录。
- 生产进度状态。

### 第三阶段：销售驱动计划

建议新增：

- 销售订单。
- 拼多多、抖音订单导入。
- 订单锁定成品库存。
- 根据未发货订单倒推生产需求。

### 第四阶段：更智能的采购

建议新增：

- 安全库存。
- 采购提前期。
- 日均销量。
- 需求预测。
- 多供应商报价。
- 采购审批。
- 自动生成待确认采购单。

### 第五阶段：平台协同

建议新增：

- 拼多多订单同步。
- 抖音订单同步。
- 店铺 SKU 和系统成品 SKU 映射。
- 平台订单自动锁库存。
- 缺货自动提醒。
