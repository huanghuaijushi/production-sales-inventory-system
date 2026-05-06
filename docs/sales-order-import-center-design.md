# 销售订单导入中心开发设计文档

## 1. 背景与目标

当前系统已经具备较完整的销售、库存和批次出库链路：

- `sales_order`：正式销售订单
- `sales_order_item`：正式销售订单明细
- `stock`：商品总库存，包含锁定库存
- `stock_batch`：批次库存，用于发货时按批次扣减
- `stock_record`：库存流水，用于记录销售出库、生产入库、采购入库等库存变动
- `product`：系统商品主数据

现有销售单创建后会锁定成品库存，发货时按批次扣减库存并生成销售出库流水。这个主链路是合理的，后续新增外部订单导入能力时，不应绕开或重写这套正式销售逻辑。

当前主要问题是 `sales_order.channel` 使用固定枚举：

- `DOUYIN`
- `PINDUODUO`
- `OFFLINE`

随着后续增加微信群、私域、微信小店、合同客户、API 对接等来源，固定枚举会越来越难维护，也无法承载不同渠道的解析规则、商品映射规则和导入审计信息。

因此新增“销售订单导入中心”，用于承接外部渠道订单。外部订单先进入导入中间层，经过解析、商品匹配、预览校验和人工确认后，才生成正式 `sales_order`，并复用现有库存锁定和发货出库逻辑。

目标流程：

```text
外部订单导入 / 文本粘贴 / 手工录入 / API 接入
  -> 生成导入批次
  -> 解析为外部原始订单
  -> 商品映射匹配
  -> 预览校验
  -> 人工确认
  -> 生成正式 sales_order
  -> 锁定库存
  -> 发货出库
  -> 生成 stock_record
```

核心原则：

1. 外部订单不直接影响库存。
2. 只有正式 `sales_order` 才参与库存锁定、发货和库存流水。
3. 外部原始数据必须保留，便于追溯。
4. 商品匹配必须可人工干预、可复用、可审计。
5. 转正式销售单必须具备幂等能力，避免重复转单。
6. 旧销售单逻辑短期保持兼容，逐步迁移到渠道表。

---

## 2. 当前销售链路分析

### 2.1 现有数据表

当前迁移文件中销售相关核心表如下：

| 表名 | 职责 |
| --- | --- |
| `sales_order` | 正式销售订单，包含渠道、客户、金额、状态、下单时间等信息 |
| `sales_order_item` | 销售订单明细，包含商品快照、数量、单价、小计 |
| `stock` | 商品总库存，包含当前库存、锁定库存、可用库存 |
| `stock_batch` | 商品批次库存，用于发货按批次扣减 |
| `stock_record` | 出入库流水，销售发货时生成 `OUT / SALES` 流水 |
| `product` | 商品主数据，区分成品和原料 |

### 2.2 现有订单状态

`sales_order.status` 当前包含：

- `PENDING`：待发货
- `SHIPPED`：已发货
- `COMPLETED`：已完成
- `CANCELLED`：已取消

这套状态可以继续保留。导入中心只负责生成 `PENDING` 状态的正式销售单，后续发货、完成、取消继续走现有销售单逻辑。

### 2.3 当前不足

1. 销售渠道是枚举字段，扩展性不足。
2. 缺少外部订单原始数据存储。
3. 无法记录外部订单商品名、规格名与系统商品的匹配过程。
4. 无法防止同一外部订单重复导入。
5. 不支持批量导入预览和确认。
6. 不便于接入微信群文本、拼多多 Excel、微信小店 API、合同客户等多来源订单。

---

## 3. 总体设计

新增“销售订单导入中心”作为外部订单和正式销售单之间的中间层。

### 3.1 数据分层

```text
渠道配置层
  sales_channel

导入任务层
  order_import_batch

外部原始订单层
  external_order_raw
  external_order_item_raw

商品匹配规则层
  channel_product_mapping

正式销售层
  sales_order
  sales_order_item

库存层
  stock
  stock_batch
  stock_record
```

### 3.2 数据流转

```text
sales_channel
  -> order_import_batch
  -> external_order_raw
  -> external_order_item_raw
  -> channel_product_mapping 匹配
  -> 人工确认
  -> sales_order / sales_order_item
  -> stock 锁库存
  -> stock_batch 发货扣批次
  -> stock_record 销售出库流水
```

### 3.3 模块边界

导入中心负责：

- 渠道管理
- 导入批次管理
- 文件 / 文本解析
- 原始订单存储
- 商品匹配
- 导入预览
- 转正式销售单

现有销售模块继续负责：

- 正式销售单查询
- 销售单创建
- 锁定库存
- 发货出库
- 取消订单释放锁定库存
- 完成订单

库存模块继续负责：

- 库存锁定
- 批次出库
- 库存流水
- 可用库存计算

---

## 4. 数据库设计

## 4.1 新增表：`sales_channel`

用途：维护销售渠道，替代固定枚举渠道。

建议字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `code` | `varchar(50)` | 渠道编码，如 `PINDUODUO`、`WECHAT_GROUP`、`DOUYIN`、`OFFLINE`、`CONTRACT` |
| `name` | `varchar(80)` | 渠道名称，如拼多多、微信群、抖店 |
| `source_type` | `varchar(30)` | 默认来源类型：`EXCEL`、`TEXT`、`MANUAL`、`CONTRACT`、`API` |
| `enabled` | `tinyint(1)` | 是否启用 |
| `sort_order` | `int` | 排序 |
| `config_json` | `json` 或 `text` | 渠道扩展配置，如模板版本、解析规则、默认物流等 |
| `remark` | `varchar(255)` | 备注 |
| `created_at` | `datetime(6)` | 创建时间 |
| `updated_at` | `datetime(6)` | 更新时间 |

建议约束：

- `uk_sales_channel_code(code)`
- `ck_sales_channel_source_type(source_type in ('EXCEL','TEXT','MANUAL','CONTRACT','API'))`
- `ck_sales_channel_enabled(enabled in (0,1))`

初始化建议：

- `PINDUODUO`：拼多多，`EXCEL`
- `WECHAT_GROUP`：微信群，`TEXT`
- `DOUYIN`：抖店，`EXCEL` 或 `API`
- `OFFLINE`：线下，`MANUAL`
- `CONTRACT`：合同客户，`CONTRACT`

---

## 4.2 新增表：`order_import_batch`

用途：记录一次导入任务。一次 Excel 上传、一次微信群文本粘贴、一次 API 拉单都对应一个导入批次。

建议字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `batch_no` | `varchar(64)` | 导入批次号，系统生成 |
| `channel_id` | `bigint` | 销售渠道 ID |
| `source_type` | `varchar(30)` | 本次导入来源：`EXCEL`、`TEXT`、`MANUAL`、`CONTRACT`、`API` |
| `file_name` | `varchar(255)` | 原始文件名，文本导入可为空 |
| `raw_text` | `longtext` | 文本导入原文，Excel 导入可为空 |
| `total_count` | `int` | 解析出的订单总数 |
| `parsed_count` | `int` | 已解析订单数 |
| `ready_count` | `int` | 已匹配且可确认订单数 |
| `converted_count` | `int` | 已转正式销售单订单数 |
| `error_count` | `int` | 异常订单数 |
| `status` | `varchar(30)` | 批次状态 |
| `operator_id` | `bigint` | 操作人 ID |
| `operator_name` | `varchar(80)` | 操作人名称 |
| `confirmed_at` | `datetime(6)` | 确认时间 |
| `created_at` | `datetime(6)` | 创建时间 |
| `updated_at` | `datetime(6)` | 更新时间 |

批次状态建议：

- `DRAFT`：草稿，刚创建但未解析
- `PARSED`：已解析
- `READY`：全部或部分订单已满足确认条件
- `CONFIRMING`：正在确认转单
- `CONFIRMED`：已确认完成
- `PARTIAL_CONFIRMED`：部分确认成功，部分失败
- `CANCELLED`：已取消

建议约束和索引：

- `uk_order_import_batch_no(batch_no)`
- `idx_order_import_batch_channel(channel_id)`
- `idx_order_import_batch_status(status)`
- `idx_order_import_batch_created_at(created_at)`
- 外键 `channel_id` -> `sales_channel(id)`
- 外键 `operator_id` -> `admin_user(id)`

说明：

`success_count / failed_count` 容易产生语义歧义。建议使用 `parsed_count / ready_count / converted_count / error_count`，分别表示解析、匹配准备、转单、错误数量。

---

## 4.3 新增表：`external_order_raw`

用途：保存外部原始订单和解析后的标准化订单字段，不直接扣库存。

建议字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `batch_id` | `bigint` | 导入批次 ID |
| `channel_id` | `bigint` | 渠道 ID |
| `external_order_no` | `varchar(100)` | 外部订单号 |
| `raw_payload` | `json` 或 `longtext` | 原始 JSON 快照 |
| `customer_name` | `varchar(120)` | 客户名称 |
| `customer_phone` | `varchar(30)` | 客户电话 |
| `customer_address` | `varchar(500)` | 收货地址 |
| `province` | `varchar(50)` | 省 |
| `city` | `varchar(50)` | 市 |
| `district` | `varchar(50)` | 区 / 县 |
| `logistics_company` | `varchar(80)` | 物流公司 |
| `tracking_no` | `varchar(100)` | 物流单号 |
| `buyer_message` | `varchar(500)` | 买家留言 |
| `seller_remark` | `varchar(500)` | 商家备注 |
| `order_time` | `datetime(6)` | 外部下单时间 |
| `paid_at` | `datetime(6)` | 外部支付时间，可为空 |
| `total_amount` | `decimal(12,2)` | 外部订单金额 |
| `status` | `varchar(30)` | 原始订单处理状态 |
| `error_message` | `varchar(1000)` | 错误信息 |
| `sales_order_id` | `bigint` | 转成的正式销售单 ID |
| `converted_at` | `datetime(6)` | 转单时间 |
| `created_at` | `datetime(6)` | 创建时间 |
| `updated_at` | `datetime(6)` | 更新时间 |

原始订单状态建议：

- `WAIT_PARSE`：待解析
- `WAIT_MATCH`：待商品匹配
- `READY`：已匹配，可确认
- `ERROR`：解析或校验异常
- `CONVERTED`：已转正式销售单
- `SKIPPED`：人工跳过
- `DUPLICATED`：重复订单

建议约束和索引：

- `uk_external_order_channel_no(channel_id, external_order_no)`
- `idx_external_order_batch(batch_id)`
- `idx_external_order_channel(channel_id)`
- `idx_external_order_status(status)`
- `idx_external_order_sales_order(sales_order_id)`
- 外键 `batch_id` -> `order_import_batch(id)`
- 外键 `channel_id` -> `sales_channel(id)`
- 外键 `sales_order_id` -> `sales_order(id)`，建议 `ON DELETE SET NULL`

关键说明：

`uk_external_order_channel_no(channel_id, external_order_no)` 是防重复导入的核心约束，必须保留。同一外部渠道的同一订单号只能导入一次。

---

## 4.4 新增表：`external_order_item_raw`

用途：保存外部原始订单商品明细，并记录匹配到系统商品的结果。

建议字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `external_order_id` | `bigint` | 外部原始订单 ID |
| `external_product_name` | `varchar(255)` | 外部商品名称 |
| `external_spec_name` | `varchar(255)` | 外部规格名称 |
| `external_sku_code` | `varchar(100)` | 外部 SKU 编码 |
| `external_quantity` | `decimal(12,4)` | 外部订单数量 |
| `external_unit_price` | `decimal(12,2)` | 外部单价 |
| `external_amount` | `decimal(12,2)` | 外部明细金额 |
| `matched_product_id` | `bigint` | 匹配到的系统商品 ID |
| `matched_product_code` | `varchar(64)` | 匹配商品编码快照 |
| `matched_product_name` | `varchar(120)` | 匹配商品名称快照 |
| `matched_product_specification` | `varchar(64)` | 匹配商品规格快照 |
| `matched_product_unit` | `varchar(20)` | 匹配商品单位快照 |
| `mapping_id` | `bigint` | 命中的商品映射规则 ID |
| `converted_quantity` | `int` | 换算后的系统销售数量 |
| `match_status` | `varchar(30)` | 匹配状态 |
| `match_message` | `varchar(500)` | 匹配说明 |
| `created_at` | `datetime(6)` | 创建时间 |
| `updated_at` | `datetime(6)` | 更新时间 |

匹配状态建议：

- `MATCHED`：已匹配
- `UNMATCHED`：未匹配
- `AMBIGUOUS`：匹配到多条规则或多个商品，存在歧义
- `DISABLED_MAPPING`：存在匹配规则但规则已停用
- `INVALID_QUANTITY`：数量换算异常

建议约束和索引：

- `idx_external_order_item_order(external_order_id)`
- `idx_external_order_item_matched_product(matched_product_id)`
- `idx_external_order_item_match_status(match_status)`
- `idx_external_order_item_mapping(mapping_id)`
- 外键 `external_order_id` -> `external_order_raw(id)`，建议 `ON DELETE CASCADE`
- 外键 `matched_product_id` -> `product(id)`，建议 `ON DELETE RESTRICT`
- 外键 `mapping_id` -> `channel_product_mapping(id)`，建议 `ON DELETE SET NULL`

---

## 4.5 新增表：`channel_product_mapping`

用途：维护不同渠道外部商品与系统商品的映射规则。

建议字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `id` | `bigint` | 主键 |
| `channel_id` | `bigint` | 渠道 ID |
| `external_product_name` | `varchar(255)` | 外部商品名称 |
| `external_spec_name` | `varchar(255)` | 外部规格名称 |
| `external_sku_code` | `varchar(100)` | 外部 SKU 编码 |
| `match_type` | `varchar(30)` | 匹配方式 |
| `match_pattern` | `varchar(500)` | 模糊匹配表达式或关键字，第一版可为空 |
| `product_id` | `bigint` | 系统商品 ID |
| `quantity_multiplier` | `decimal(12,4)` | 数量换算倍率 |
| `enabled` | `tinyint(1)` | 是否启用 |
| `priority` | `int` | 优先级，数字越大优先级越高 |
| `remark` | `varchar(500)` | 备注 |
| `created_at` | `datetime(6)` | 创建时间 |
| `updated_at` | `datetime(6)` | 更新时间 |

匹配方式建议：

- `EXACT`：精确匹配，第一版优先支持
- `SKU`：按外部 SKU 匹配
- `KEYWORD`：关键词匹配，后续支持
- `REGEX`：正则匹配，后续支持
- `MANUAL`：人工建立规则

建议约束和索引：

- `idx_channel_product_mapping_channel(channel_id)`
- `idx_channel_product_mapping_product(product_id)`
- `idx_channel_product_mapping_enabled(enabled)`
- `idx_channel_product_mapping_sku(channel_id, external_sku_code)`
- `idx_channel_product_mapping_name_spec(channel_id, external_product_name, external_spec_name)`
- 外键 `channel_id` -> `sales_channel(id)`
- 外键 `product_id` -> `product(id)`
- 检查 `quantity_multiplier > 0`
- 检查 `enabled in (0,1)`

是否设置唯一约束：

第一版可以考虑增加较宽松的唯一约束：

```text
(channel_id, external_product_name, external_spec_name, external_sku_code, product_id)
```

但需要注意 MySQL 中 `NULL` 参与唯一约束的行为。如果外部规格或 SKU 经常为空，更建议在服务层做重复校验，而不是完全依赖数据库唯一约束。

示例：

```text
渠道：拼多多
外部商品：蛋黄鲜肉粽
外部规格：188g*10
系统商品：蛋黄鲜肉粽
quantity_multiplier：10
```

表示外部订单里买 1 件该规格，对应系统销售 `蛋黄鲜肉粽` 10 个。

---

## 4.6 改造表：`sales_order`

建议在现有正式销售单上新增导入来源字段。

新增字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `channel_id` | `bigint` | 新渠道 ID |
| `external_order_no` | `varchar(100)` | 外部订单号 |
| `import_batch_id` | `bigint` | 导入批次 ID |
| `source_type` | `varchar(30)` | 来源类型：`EXCEL`、`TEXT`、`MANUAL`、`CONTRACT`、`API` |
| `source_remark` | `varchar(500)` | 来源备注 |

建议索引：

- `idx_sales_order_channel_id(channel_id)`
- `idx_sales_order_external_order(channel_id, external_order_no)`
- `idx_sales_order_import_batch(import_batch_id)`

兼容策略：

1. 短期保留原有 `channel` 字段，不删除。
2. 新导入订单同时写入 `channel_id` 和兼容用 `channel`。
3. 新接口优先使用 `channel_id`。
4. 前端展示时优先展示 `sales_channel.name`，如果为空则回退展示旧 `channel`。
5. 后续确认所有逻辑迁移完成后，再考虑废弃 `channel` 枚举约束。

注意：

当前 `sales_order.channel` 有检查约束，只允许 `DOUYIN / PINDUODUO / OFFLINE`。因此第一阶段如果要支持 `WECHAT_GROUP / CONTRACT`，需要调整该检查约束，或者在兼容字段中临时写入 `OFFLINE` 并通过 `channel_id` 表示真实渠道。更推荐迁移时放宽或删除旧 `channel` 的检查约束。

---

## 4.7 改造表：`sales_order_item`

建议在正式销售明细中增加外部商品快照字段，方便追溯。

新增字段：

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| `external_product_name` | `varchar(255)` | 外部商品名称快照 |
| `external_spec_name` | `varchar(255)` | 外部规格名称快照 |
| `external_sku_code` | `varchar(100)` | 外部 SKU 编码快照 |
| `external_quantity` | `decimal(12,4)` | 外部数量 |
| `external_unit_price` | `decimal(12,2)` | 外部单价 |
| `mapping_id` | `bigint` | 使用的商品映射规则 ID |
| `external_order_item_id` | `bigint` | 来源外部订单明细 ID |

建议索引：

- `idx_sales_order_item_mapping(mapping_id)`
- `idx_sales_order_item_external_item(external_order_item_id)`

说明：

这些字段是追溯字段，不参与正式库存扣减。正式库存扣减仍然使用 `product_id` 和 `quantity`。

---

## 5. 状态流转设计

### 5.1 导入批次状态流转

```text
DRAFT
  -> PARSED
  -> READY
  -> CONFIRMING
  -> CONFIRMED
```

异常或部分成功路径：

```text
DRAFT / PARSED / READY
  -> CANCELLED

CONFIRMING
  -> PARTIAL_CONFIRMED
  -> CONFIRMED
```

状态说明：

| 状态 | 说明 |
| --- | --- |
| `DRAFT` | 批次已创建，还未解析 |
| `PARSED` | 已解析出外部订单 |
| `READY` | 已完成商品匹配，存在可确认订单 |
| `CONFIRMING` | 正在批量转正式销售单 |
| `CONFIRMED` | 批次内订单全部处理完成 |
| `PARTIAL_CONFIRMED` | 部分订单转单成功，部分失败或跳过 |
| `CANCELLED` | 批次取消，不允许继续确认 |

### 5.2 外部原始订单状态流转

```text
WAIT_PARSE
  -> WAIT_MATCH
  -> READY
  -> CONVERTED
```

异常或人工处理路径：

```text
WAIT_PARSE / WAIT_MATCH / READY
  -> ERROR
  -> SKIPPED
  -> DUPLICATED
```

状态说明：

| 状态 | 说明 |
| --- | --- |
| `WAIT_PARSE` | 等待解析 |
| `WAIT_MATCH` | 已解析，等待商品匹配 |
| `READY` | 商品全部匹配，可转正式销售单 |
| `ERROR` | 解析、校验或匹配异常 |
| `CONVERTED` | 已生成正式销售单 |
| `SKIPPED` | 人工跳过，不转正式销售单 |
| `DUPLICATED` | 重复外部订单 |

### 5.3 明细匹配状态流转

```text
UNMATCHED
  -> MATCHED
```

异常路径：

```text
UNMATCHED
  -> AMBIGUOUS
  -> DISABLED_MAPPING
  -> INVALID_QUANTITY
```

---

## 6. 商品匹配规则

### 6.1 第一版匹配优先级

第一阶段建议只做简单、可控、稳定的匹配规则：

1. 同渠道下，按 `external_sku_code` 精确匹配启用规则。
2. 如果没有 SKU，则按 `external_product_name + external_spec_name` 精确匹配启用规则。
3. 如果命中多条，选择 `priority` 最高的一条。
4. 如果最高优先级仍有多条，标记为 `AMBIGUOUS`。
5. 如果未命中，标记为 `UNMATCHED`。
6. 如果命中停用规则，标记为 `DISABLED_MAPPING`。
7. 按 `quantity_multiplier` 计算 `converted_quantity`。

换算公式：

```text
converted_quantity = external_quantity * quantity_multiplier
```

如果结果不是整数，第一版建议标记为 `INVALID_QUANTITY`，由人工处理。

### 6.2 一键建立映射

在导入预览页面，如果某个外部商品未匹配，业务人员可以选择系统商品并建立映射。

建立映射时建议默认填充：

- `channel_id`：当前批次渠道
- `external_product_name`：外部商品名
- `external_spec_name`：外部规格名
- `external_sku_code`：外部 SKU
- `product_id`：人工选择的系统商品
- `quantity_multiplier`：默认 1，可手动调整
- `match_type`：`MANUAL` 或 `EXACT`
- `enabled`：1
- `priority`：100

保存后重新匹配当前批次中相同外部商品。

### 6.3 后续增强

后续可支持：

- 关键词匹配
- 正则匹配
- 规格数量自动识别，如 `*10`、`x10`、`10枚装`
- 同义词维护
- 映射规则导入 / 导出
- 映射命中率统计

---

## 7. 转正式销售单逻辑

### 7.1 确认前校验

外部订单必须满足以下条件才允许转正式销售单：

1. 状态为 `READY`。
2. 外部订单号不为空。
3. 收货人、电话、地址根据业务要求校验，第一版可允许部分为空但需提示。
4. 所有明细均为 `MATCHED`。
5. 所有明细 `converted_quantity > 0`。
6. 对应系统商品必须启用。
7. 当前没有 `sales_order_id`。
8. 同一 `channel_id + external_order_no` 未生成过正式销售单。

### 7.2 转单字段映射

外部原始订单 -> 正式销售单：

| 外部原始订单 | 正式销售单 |
| --- | --- |
| `external_order_no` | `external_order_no` |
| `channel_id` | `channel_id` |
| `batch_id` | `import_batch_id` |
| `customer_name` | `customer_name` |
| `customer_phone` | `customer_phone` |
| `customer_address` | `customer_address` |
| `total_amount` | `total_amount` |
| `order_time` | `order_date` |
| `buyer_message / seller_remark` | `remark` 或 `source_remark` |

外部原始明细 -> 正式销售明细：

| 外部明细 | 正式销售明细 |
| --- | --- |
| `matched_product_id` | `product_id` |
| `matched_product_code` | `product_code` |
| `matched_product_name` | `product_name` |
| `matched_product_specification` | `product_specification` |
| `matched_product_unit` | `product_unit` |
| `converted_quantity` | `quantity` |
| `external_unit_price` | `unit_price`，或按换算后重新计算 |
| `external_product_name` | `external_product_name` |
| `external_spec_name` | `external_spec_name` |
| `external_quantity` | `external_quantity` |
| `mapping_id` | `mapping_id` |

### 7.3 金额处理建议

第一版建议采用简单规则：

- 如果外部明细有金额，则销售明细 `subtotal` 使用外部明细金额。
- 如果只有外部单价和外部数量，则 `subtotal = external_unit_price * external_quantity`。
- 正式销售单 `total_amount` 优先使用外部订单总金额。
- 如果外部订单没有总金额，则按正式明细小计汇总。

注意：

外部规格可能是“10 个装”，系统明细 `quantity` 是换算后的 10 个。此时 `unit_price` 应该是系统单位口径的价格，否则报表单价会失真。

建议：

```text
正式明细 unit_price = external_amount / converted_quantity
正式明细 subtotal = external_amount
```

如果 `external_amount` 为空，则使用系统商品默认销售价计算。

### 7.4 库存锁定

导入中心不直接锁库存。转成正式销售单时，调用现有销售单创建逻辑，由现有逻辑完成库存锁定。

这样可以保证：

- 手工销售单和导入销售单库存逻辑一致
- 后续取消订单释放库存逻辑一致
- 发货批次出库逻辑一致
- 库存流水口径一致

---

## 8. 幂等与并发控制

### 8.1 防重复导入

通过 `external_order_raw` 的唯一约束防止重复导入：

```text
uk_external_order_channel_no(channel_id, external_order_no)
```

如果再次导入相同渠道同一外部订单号：

- 不再新增原始订单
- 可标记为 `DUPLICATED`
- 在导入预览中提示“已导入”

### 8.2 防重复转单

转正式销售单时必须做幂等控制：

1. 对 `external_order_raw` 加事务锁或乐观判断。
2. 如果 `sales_order_id` 不为空，直接跳过。
3. 如果状态是 `CONVERTED`，直接跳过。
4. 创建正式销售单成功后，立即回写 `sales_order_id`、`converted_at`、`status = CONVERTED`。
5. 如果中途失败，保持原始订单为 `ERROR` 或 `READY`，记录错误信息。

建议在 `sales_order` 上也增加索引：

```text
(channel_id, external_order_no)
```

服务层确认时再校验是否已经存在相同来源正式销售单。

### 8.3 批量确认事务边界

批量确认不建议整个批次使用一个大事务。推荐每个外部订单一个事务。

原因：

- 某一单库存不足，不应导致整个批次回滚。
- 某一单数据异常，不应影响其他订单转单。
- 大批量导入时长事务容易锁表和超时。

推荐策略：

```text
确认批次
  -> 批次状态改为 CONFIRMING
  -> 遍历 READY 订单
      -> 单订单事务转正式销售单
      -> 成功标记 CONVERTED
      -> 失败标记 ERROR 并写 error_message
  -> 汇总 converted_count / error_count
  -> 更新批次状态为 CONFIRMED 或 PARTIAL_CONFIRMED
```

---

## 9. 后端模块设计

建议新增包：

```text
com.hhjs.psi.sales.importing
```

子包建议：

```text
sales/importing/controller
sales/importing/service
sales/importing/repository
sales/importing/entity
sales/importing/dto
sales/importing/parser
```

核心类建议：

| 类名 | 职责 |
| --- | --- |
| `SalesChannelService` | 渠道管理 |
| `OrderImportService` | 导入批次创建、解析、预览、确认 |
| `ExternalOrderConvertService` | 外部订单转正式销售单 |
| `ChannelProductMappingService` | 商品映射维护和匹配 |
| `PddExcelImportParser` | 拼多多 Excel 解析 |
| `WechatTextImportParser` | 微信群文本解析 |
| `ExternalOrderMatcher` | 外部订单商品匹配 |

### 9.1 Parser 接口建议

建议定义统一解析接口：

```text
OrderImportParser
  parse(input) -> ParsedExternalOrderList
```

不同渠道和来源实现不同 Parser：

- `PddExcelImportParser`
- `WechatTextImportParser`
- `ContractOrderParser`
- `ManualOrderParser`

这样后续新增微信小店 API 或抖店 API 时，不需要改导入主流程。

### 9.2 转单复用现有销售服务

`ExternalOrderConvertService` 不应直接操作库存。它应该组装现有销售单创建请求，然后调用现有 `SalesOrderService` 的创建逻辑。

如现有 `SalesOrderService` 不适合复用，建议抽出内部方法：

```text
createSalesOrderInternal(request, sourceContext)
```

由手工创建和导入创建共同调用。

---

## 10. API 设计

### 10.1 渠道接口

```text
GET    /api/v1/sales/channels
POST   /api/v1/sales/channels
PUT    /api/v1/sales/channels/{id}
PATCH  /api/v1/sales/channels/{id}/enabled
```

### 10.2 商品映射接口

```text
GET    /api/v1/sales/product-mappings
POST   /api/v1/sales/product-mappings
PUT    /api/v1/sales/product-mappings/{id}
DELETE /api/v1/sales/product-mappings/{id}
POST   /api/v1/sales/product-mappings/match-preview
```

查询条件建议支持：

- `channelId`
- `keyword`
- `enabled`
- `matchType`

### 10.3 导入接口

```text
POST /api/v1/sales/imports/pdd-excel
POST /api/v1/sales/imports/wechat-text
GET  /api/v1/sales/imports
GET  /api/v1/sales/imports/{batchId}
POST /api/v1/sales/imports/{batchId}/parse
POST /api/v1/sales/imports/{batchId}/rematch
POST /api/v1/sales/imports/{batchId}/preview
POST /api/v1/sales/imports/{batchId}/confirm
POST /api/v1/sales/imports/{batchId}/cancel
```

### 10.4 外部订单处理接口

```text
GET  /api/v1/sales/imports/{batchId}/orders
GET  /api/v1/sales/external-orders/{id}
POST /api/v1/sales/external-orders/{id}/skip
POST /api/v1/sales/external-orders/{id}/rematch
POST /api/v1/sales/external-orders/{id}/convert
```

### 10.5 一键建立映射接口

```text
POST /api/v1/sales/external-order-items/{itemId}/create-mapping
```

请求内容建议：

- `productId`
- `quantityMultiplier`
- `matchType`
- `priority`
- `remark`

创建映射后自动重新匹配同批次未匹配明细。

---

## 11. 前端页面设计

销售管理页面建议拆成四个 Tab：

```text
销售单
订单导入
商品匹配
渠道设置
```

### 11.1 销售单 Tab

继续展示正式 `sales_order`。新增字段展示：

- 渠道名称
- 外部订单号
- 导入批次号
- 来源类型

### 11.2 订单导入 Tab

建议包含两个层级。

#### 批次列表

展示字段：

- 批次号
- 渠道
- 来源类型
- 文件名 / 来源摘要
- 总订单数
- 可确认数
- 已转单数
- 异常数
- 状态
- 操作人
- 创建时间

操作：

- 上传拼多多 Excel
- 粘贴微信群文本
- 查看详情
- 重新匹配
- 确认生成销售单
- 取消批次

#### 批次详情

展示订单级预览：

- 外部订单号
- 客户信息
- 商品数量
- 匹配状态
- 错误信息
- 是否已转销售单

展开后展示商品明细：

- 外部商品名
- 外部规格
- 外部 SKU
- 外部数量
- 匹配商品
- 换算数量
- 匹配状态
- 操作：选择商品、建立映射、重新匹配

视觉建议：

- 未匹配商品标红
- 歧义匹配标橙
- 已匹配标绿
- 已转单置灰，禁止重复操作

### 11.3 商品匹配 Tab

用于维护 `channel_product_mapping`。

功能：

- 按渠道筛选
- 按外部商品名搜索
- 新增映射
- 编辑映射
- 启用 / 停用
- 设置优先级
- 调整数量换算倍率

### 11.4 渠道设置 Tab

用于维护 `sales_channel`。

功能：

- 新增渠道
- 编辑渠道
- 启用 / 停用渠道
- 配置默认来源类型
- 配置渠道备注和扩展配置

---

## 12. 拼多多 Excel 导入设计

### 12.1 第一版目标

第一版优先支持用户上传拼多多订单 Excel 文件，系统解析为外部原始订单。

### 12.2 解析建议

解析时需要做字段映射配置，避免模板列名变化导致代码大量修改。

建议先支持以下字段：

- 外部订单号
- 商品名称
- 商品规格
- SKU 编码
- 购买数量
- 商品单价
- 商品金额
- 收货人
- 收货电话
- 收货地址
- 买家留言
- 商家备注
- 下单时间
- 支付时间

### 12.3 容错策略

- 订单号为空：整行错误
- 商品名为空：明细错误
- 数量为空或小于等于 0：明细错误
- 地址为空：订单警告，第一版可允许导入但预览提示
- 同一订单号多行：合并为同一个 `external_order_raw`，多条 `external_order_item_raw`
- 重复导入同一订单号：标记重复，不生成新订单

---

## 13. 微信群文本导入设计

### 13.1 第一版目标

支持将微信群订单文本粘贴到系统，解析为外部原始订单。

### 13.2 建议输入格式

第一版不建议直接支持过于自由的聊天记录。建议先约定标准格式，例如：

```text
姓名：张三
电话：13800000000
地址：浙江省杭州市西湖区xxx
商品：蛋黄鲜肉粽 10个
备注：周五前发货
---
姓名：李四
电话：13900000000
地址：上海市浦东新区xxx
商品：豆沙粽 5个；鲜肉粽 5个
```

### 13.3 解析策略

- 用 `---` 分隔多个订单
- 识别姓名、电话、地址、商品、备注
- 商品支持 `；`、`,`、换行分隔
- 商品数量用简单正则识别，如 `10个`、`x10`、`*10`
- 无法识别的商品进入 `UNMATCHED`

### 13.4 后续增强

后续可增加自由文本智能解析，但第一版应优先保证可控格式的准确性。

---

## 14. 一期实现范围

建议第一阶段实现最小闭环：

1. 新增 `sales_channel`。
2. 新增 `order_import_batch`。
3. 新增 `external_order_raw`。
4. 新增 `external_order_item_raw`。
5. 新增 `channel_product_mapping`。
6. 改造 `sales_order`，增加导入来源字段。
7. 改造 `sales_order_item`，增加外部商品快照字段。
8. 实现渠道设置页面。
9. 实现商品映射维护页面。
10. 实现拼多多 Excel 上传解析。
11. 实现微信群文本粘贴解析。
12. 实现导入批次预览。
13. 实现未匹配商品一键建立映射。
14. 实现批量确认生成正式销售单。
15. 复用现有销售单锁库存逻辑。
16. 保留现有销售单列表和发货逻辑。

一期不建议做：

- 自动调用外部平台 API 拉单
- 复杂正则 / AI 自由文本解析
- 自动确认无需人工审核
- 发货单自动回传平台
- 复杂对账
- 售后退款

---

## 15. 建议实施顺序

### 第一步：数据库迁移

新增迁移文件，例如：

```text
V3__sales_order_import_center.sql
```

内容包括：

- 新增 5 张导入中心表
- 修改 `sales_order`
- 修改 `sales_order_item`
- 初始化基础渠道数据
- 处理旧 `sales_order.channel` 兼容约束

### 第二步：后端实体和 Repository

新增：

- `SalesChannel`
- `OrderImportBatch`
- `ExternalOrderRaw`
- `ExternalOrderItemRaw`
- `ChannelProductMapping`

### 第三步：渠道和映射 CRUD

先完成：

- 渠道列表 / 新增 / 编辑 / 启停
- 商品映射列表 / 新增 / 编辑 / 删除 / 启停

### 第四步：导入解析

先完成两个 Parser：

- 拼多多 Excel
- 微信群标准文本

### 第五步：商品匹配

完成：

- 自动匹配
- 手工建立映射
- 重新匹配

### 第六步：确认转单

完成：

- 预览校验
- 单订单事务转单
- 幂等控制
- 回写转单状态
- 调用现有库存锁定逻辑

### 第七步：前端页面

按 Tab 逐步补齐：

1. 渠道设置
2. 商品匹配
3. 订单导入列表
4. 订单导入详情
5. 确认转单

---

## 16. 风险与注意事项

### 16.1 旧渠道枚举约束

当前 `sales_order.channel` 有检查约束，只允许旧渠道。如果新增微信群、合同客户等渠道，必须处理这个约束。

建议第一阶段方案：

- 新增 `channel_id` 承载真实渠道。
- 放宽或移除 `sales_order.channel` 的检查约束。
- 保留 `channel` 字段作为兼容展示字段。

### 16.2 金额口径

外部订单可能按“套装”计价，系统按“单个成品”计库存。需要明确正式销售明细的单价口径。

建议：

- 库存数量以 `converted_quantity` 为准。
- 金额以外部订单金额为准。
- 系统单位单价由 `external_amount / converted_quantity` 计算。

### 16.3 库存不足

确认转单时可能发生库存不足。

建议：

- 单个订单转单失败，不影响其他订单。
- 失败订单保持 `READY` 或转 `ERROR`，并记录库存不足原因。
- 前端预览时提前展示库存可用量和预计锁定量。

### 16.4 地址质量

微信群文本订单地址可能不规范。

第一版建议只做轻量校验：

- 收货人为空提示
- 电话为空或格式异常提示
- 地址为空提示

不要因为地址无法拆省市区就阻止导入，除非业务明确要求。

### 16.5 原始数据保留

`raw_payload` 和 `raw_text` 必须保留，便于以后排查导入错误。

### 16.6 批量事务

批量确认不要使用一个大事务。应按订单粒度提交，避免单个异常导致整批失败。

---

## 17. 后续扩展方向

1. 微信小店 API 拉单。
2. 抖店 API 拉单。
3. 合同订单模板导入。
4. 发货后回填物流单号到外部平台。
5. 售后退款和退货入库。
6. 渠道销售报表。
7. 商品映射命中率分析。
8. 外部订单重复导入智能合并。
9. 订单地址智能清洗。
10. 导入错误明细表和操作日志表。

---

## 18. 总结

销售订单导入中心应定位为“外部订单进入正式销售系统前的缓冲层和治理层”。

它不替代现有销售单，也不直接处理库存，而是负责：

- 接收外部订单
- 保留原始数据
- 解析和标准化
- 匹配系统商品
- 人工预览确认
- 幂等转正式销售单

正式销售单生成后，继续复用现有库存锁定、发货出库和库存流水逻辑。这样既能保持当前系统稳定，又能为后续多渠道订单接入打好基础。
