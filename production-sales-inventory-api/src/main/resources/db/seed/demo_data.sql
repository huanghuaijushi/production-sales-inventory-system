-- Demo data for local/testing databases.
-- Apply after Flyway V1, for example:
-- mysql -h127.0.0.1 -uroot -p123456 production_sales_inventory < production-sales-inventory-api/src/main/resources/db/seed/demo_data.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO sys_user (id, username, password_hash, nickname, role, status)
VALUES (1, 'admin', '$2a$10$8FqKkXoZ9Y1xZr9DEMOHASHFORLOCALONLY', '系统管理员', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = VALUES(status);

INSERT INTO product (code, name, type, category, specification, unit, cost_price, alert_quantity, shelf_life_days, description, enabled)
VALUES
  ('FP-ZONG-DHXR-188', '蛋黄鲜肉粽', 'FINISHED_PRODUCT', '粽子', '188g/个', '个', 4.80, 120, 180, '经典蛋黄鲜肉粽成品库存', 1),
  ('FP-ZONG-HSLXR-188', '黑松露鲜肉粽', 'FINISHED_PRODUCT', '粽子', '188g/个', '个', 7.60, 80, 180, '黑松露鲜肉粽成品库存', 1),
  ('FP-ZONG-SJCPDS-160', '水晶陈皮豆沙粽', 'FINISHED_PRODUCT', '粽子', '160g/个', '个', 4.20, 100, 180, '水晶陈皮豆沙粽成品库存', 1),
  ('RM-RICE-NM-25KG', '圆粒糯米', 'RAW_MATERIAL', '原料', '25kg/袋', 'kg', 7.20, 300, 365, '粽子主料糯米', 1),
  ('RM-PORK-WH-1KG', '五花鲜肉', 'RAW_MATERIAL', '原料', '1kg/袋', 'kg', 38.00, 80, 7, '鲜肉粽用五花肉', 1),
  ('RM-EGG-YOLK-20G', '咸蛋黄', 'RAW_MATERIAL', '原料', '20g/粒', '粒', 1.35, 300, 180, '蛋黄鲜肉粽用咸蛋黄', 1),
  ('RM-TRUFFLE-100G', '黑松露酱', 'RAW_MATERIAL', '原料', '100g/瓶', 'g', 0.42, 800, 365, '黑松露鲜肉粽风味料', 1),
  ('RM-DOUSHA-5KG', '陈皮豆沙馅', 'RAW_MATERIAL', '原料', '5kg/袋', 'kg', 18.00, 120, 180, '水晶陈皮豆沙粽馅料', 1),
  ('RM-CRYSTAL-POWDER', '水晶粉', 'RAW_MATERIAL', '原料', '10kg/袋', 'kg', 12.50, 100, 365, '水晶粽外皮原料', 1),
  ('RM-ZONG-LEAF', '箬竹粽叶', 'RAW_MATERIAL', '原料', '500片/包', '片', 0.16, 1000, 365, '粽叶', 1),
  ('RM-COTTON-THREAD', '棉线', 'RAW_MATERIAL', '包装', '100m/卷', '米', 0.03, 500, 3650, '捆扎棉线', 1),
  ('PK-GIFT-6', '6只装礼盒', 'RAW_MATERIAL', '包装', '6只/盒', '个', 5.20, 100, 3650, '粽子礼盒包装', 1),
  ('PK-GIFT-10', '10只装礼盒', 'RAW_MATERIAL', '包装', '10只/盒', '个', 7.80, 80, 3650, '家庭装礼盒包装', 1),
  ('PK-ICE-BAG', '保温冰袋', 'RAW_MATERIAL', '包装', '个', '个', 1.10, 200, 3650, '冷链保温冰袋', 1)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  type = VALUES(type),
  category = VALUES(category),
  specification = VALUES(specification),
  unit = VALUES(unit),
  cost_price = VALUES(cost_price),
  alert_quantity = VALUES(alert_quantity),
  shelf_life_days = VALUES(shelf_life_days),
  description = VALUES(description),
  enabled = VALUES(enabled);

INSERT INTO stock (product_id, quantity, locked_quantity, available_quantity)
SELECT id,
       CASE code
         WHEN 'FP-ZONG-DHXR-188' THEN 520
         WHEN 'FP-ZONG-HSLXR-188' THEN 260
         WHEN 'FP-ZONG-SJCPDS-160' THEN 380
         WHEN 'RM-RICE-NM-25KG' THEN 1800
         WHEN 'RM-PORK-WH-1KG' THEN 260
         WHEN 'RM-EGG-YOLK-20G' THEN 2800
         WHEN 'RM-TRUFFLE-100G' THEN 6500
         WHEN 'RM-DOUSHA-5KG' THEN 420
         WHEN 'RM-CRYSTAL-POWDER' THEN 360
         WHEN 'RM-ZONG-LEAF' THEN 6800
         WHEN 'RM-COTTON-THREAD' THEN 3500
         WHEN 'PK-GIFT-6' THEN 420
         WHEN 'PK-GIFT-10' THEN 260
         WHEN 'PK-ICE-BAG' THEN 900
         ELSE 0
       END,
       0,
       CASE code
         WHEN 'FP-ZONG-DHXR-188' THEN 520
         WHEN 'FP-ZONG-HSLXR-188' THEN 260
         WHEN 'FP-ZONG-SJCPDS-160' THEN 380
         WHEN 'RM-RICE-NM-25KG' THEN 1800
         WHEN 'RM-PORK-WH-1KG' THEN 260
         WHEN 'RM-EGG-YOLK-20G' THEN 2800
         WHEN 'RM-TRUFFLE-100G' THEN 6500
         WHEN 'RM-DOUSHA-5KG' THEN 420
         WHEN 'RM-CRYSTAL-POWDER' THEN 360
         WHEN 'RM-ZONG-LEAF' THEN 6800
         WHEN 'RM-COTTON-THREAD' THEN 3500
         WHEN 'PK-GIFT-6' THEN 420
         WHEN 'PK-GIFT-10' THEN 260
         WHEN 'PK-ICE-BAG' THEN 900
         ELSE 0
       END
FROM product
WHERE code IN (
  'FP-ZONG-DHXR-188','FP-ZONG-HSLXR-188','FP-ZONG-SJCPDS-160','RM-RICE-NM-25KG','RM-PORK-WH-1KG',
  'RM-EGG-YOLK-20G','RM-TRUFFLE-100G','RM-DOUSHA-5KG','RM-CRYSTAL-POWDER','RM-ZONG-LEAF',
  'RM-COTTON-THREAD','PK-GIFT-6','PK-GIFT-10','PK-ICE-BAG'
)
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  locked_quantity = VALUES(locked_quantity),
  available_quantity = VALUES(available_quantity);

INSERT INTO stock_batch (product_id, batch_no, production_date, expiry_date, quantity, available_quantity, unit_cost, source_type, source_order_no, remark)
SELECT id,
       CONCAT(code, '-20260501'),
       DATE('2026-05-01'),
       CASE WHEN shelf_life_days IS NULL THEN NULL ELSE DATE_ADD(DATE('2026-05-01'), INTERVAL shelf_life_days DAY) END,
       CASE code
         WHEN 'FP-ZONG-DHXR-188' THEN 520
         WHEN 'FP-ZONG-HSLXR-188' THEN 260
         WHEN 'FP-ZONG-SJCPDS-160' THEN 380
         WHEN 'RM-RICE-NM-25KG' THEN 1800
         WHEN 'RM-PORK-WH-1KG' THEN 260
         WHEN 'RM-EGG-YOLK-20G' THEN 2800
         WHEN 'RM-TRUFFLE-100G' THEN 6500
         WHEN 'RM-DOUSHA-5KG' THEN 420
         WHEN 'RM-CRYSTAL-POWDER' THEN 360
         WHEN 'RM-ZONG-LEAF' THEN 6800
         WHEN 'RM-COTTON-THREAD' THEN 3500
         WHEN 'PK-GIFT-6' THEN 420
         WHEN 'PK-GIFT-10' THEN 260
         WHEN 'PK-ICE-BAG' THEN 900
         ELSE 0
       END,
       CASE code
         WHEN 'FP-ZONG-DHXR-188' THEN 520
         WHEN 'FP-ZONG-HSLXR-188' THEN 260
         WHEN 'FP-ZONG-SJCPDS-160' THEN 380
         WHEN 'RM-RICE-NM-25KG' THEN 1800
         WHEN 'RM-PORK-WH-1KG' THEN 260
         WHEN 'RM-EGG-YOLK-20G' THEN 2800
         WHEN 'RM-TRUFFLE-100G' THEN 6500
         WHEN 'RM-DOUSHA-5KG' THEN 420
         WHEN 'RM-CRYSTAL-POWDER' THEN 360
         WHEN 'RM-ZONG-LEAF' THEN 6800
         WHEN 'RM-COTTON-THREAD' THEN 3500
         WHEN 'PK-GIFT-6' THEN 420
         WHEN 'PK-GIFT-10' THEN 260
         WHEN 'PK-ICE-BAG' THEN 900
         ELSE 0
       END,
       cost_price,
       CASE WHEN type = 'FINISHED_PRODUCT' THEN 'PRODUCTION_ORDER' ELSE 'PURCHASE_ORDER' END,
       'DEMO-SEED-20260501',
       '演示初始化批次'
FROM product
WHERE code IN (
  'FP-ZONG-DHXR-188','FP-ZONG-HSLXR-188','FP-ZONG-SJCPDS-160','RM-RICE-NM-25KG','RM-PORK-WH-1KG',
  'RM-EGG-YOLK-20G','RM-TRUFFLE-100G','RM-DOUSHA-5KG','RM-CRYSTAL-POWDER','RM-ZONG-LEAF',
  'RM-COTTON-THREAD','PK-GIFT-6','PK-GIFT-10','PK-ICE-BAG'
);

INSERT INTO stock_record (
  record_no, product_id, type, sub_type, quantity, cost_unit_price, cost_amount, before_quantity, after_quantity,
  batch_id, batch_no, production_date, expiry_date, operator_id, operator_name, remark
)
SELECT CONCAT('DEMO-IN-', p.code),
       p.id,
       'IN',
       CASE WHEN p.type = 'FINISHED_PRODUCT' THEN 'PRODUCTION' ELSE 'PURCHASE' END,
       sb.quantity,
       sb.unit_cost,
       sb.quantity * sb.unit_cost,
       0,
       sb.quantity,
       sb.id,
       sb.batch_no,
       sb.production_date,
       sb.expiry_date,
       1,
       'admin',
       '演示库存初始化'
FROM product p
JOIN stock_batch sb ON sb.product_id = p.id AND sb.batch_no = CONCAT(p.code, '-20260501')
WHERE p.code IN (
  'FP-ZONG-DHXR-188','FP-ZONG-HSLXR-188','FP-ZONG-SJCPDS-160','RM-RICE-NM-25KG','RM-PORK-WH-1KG',
  'RM-EGG-YOLK-20G','RM-TRUFFLE-100G','RM-DOUSHA-5KG','RM-CRYSTAL-POWDER','RM-ZONG-LEAF',
  'RM-COTTON-THREAD','PK-GIFT-6','PK-GIFT-10','PK-ICE-BAG'
);

INSERT INTO bom_item (finished_product_id, material_product_id, quantity_per_unit, loss_rate)
SELECT fp.id, rm.id, x.qty, x.loss_rate
FROM (
  SELECT 'FP-ZONG-DHXR-188' fp, 'RM-RICE-NM-25KG' rm, 0.1100 qty, 0.0300 loss_rate UNION ALL
  SELECT 'FP-ZONG-DHXR-188', 'RM-PORK-WH-1KG', 0.0550, 0.0500 UNION ALL
  SELECT 'FP-ZONG-DHXR-188', 'RM-EGG-YOLK-20G', 1.0000, 0.0200 UNION ALL
  SELECT 'FP-ZONG-DHXR-188', 'RM-ZONG-LEAF', 2.0000, 0.0300 UNION ALL
  SELECT 'FP-ZONG-DHXR-188', 'RM-COTTON-THREAD', 0.4500, 0.0200 UNION ALL
  SELECT 'FP-ZONG-HSLXR-188', 'RM-RICE-NM-25KG', 0.1080, 0.0300 UNION ALL
  SELECT 'FP-ZONG-HSLXR-188', 'RM-PORK-WH-1KG', 0.0600, 0.0500 UNION ALL
  SELECT 'FP-ZONG-HSLXR-188', 'RM-TRUFFLE-100G', 8.0000, 0.0300 UNION ALL
  SELECT 'FP-ZONG-HSLXR-188', 'RM-ZONG-LEAF', 2.0000, 0.0300 UNION ALL
  SELECT 'FP-ZONG-HSLXR-188', 'RM-COTTON-THREAD', 0.4500, 0.0200 UNION ALL
  SELECT 'FP-ZONG-SJCPDS-160', 'RM-CRYSTAL-POWDER', 0.0600, 0.0300 UNION ALL
  SELECT 'FP-ZONG-SJCPDS-160', 'RM-DOUSHA-5KG', 0.0750, 0.0400 UNION ALL
  SELECT 'FP-ZONG-SJCPDS-160', 'RM-ZONG-LEAF', 1.5000, 0.0300 UNION ALL
  SELECT 'FP-ZONG-SJCPDS-160', 'RM-COTTON-THREAD', 0.4000, 0.0200
) x
JOIN product fp ON fp.code = x.fp
JOIN product rm ON rm.code = x.rm
ON DUPLICATE KEY UPDATE quantity_per_unit = VALUES(quantity_per_unit), loss_rate = VALUES(loss_rate);

INSERT INTO production_order (
  order_no, batch_no, product_id, planned_quantity, completed_quantity, inbound_quantity, loss_quantity,
  current_step, status, planned_date, started_at, completed_at, operator_id, operator_name, remark
)
SELECT x.order_no, x.batch_no, p.id, x.planned_quantity, x.completed_quantity, x.inbound_quantity, x.loss_quantity,
       x.current_step, x.status, x.planned_date, x.started_at, x.completed_at, 1, 'admin', x.remark
FROM (
  SELECT 'PO-DEMO-DHXR-20260501' order_no, 'FP-ZONG-DHXR-188-20260501' batch_no, 'FP-ZONG-DHXR-188' product_code, 600 planned_quantity, 540 completed_quantity, 520 inbound_quantity, 20 loss_quantity, 'BOXING' current_step, 'COMPLETED' status, DATE('2026-05-01') planned_date, TIMESTAMP('2026-05-01 08:30:00') started_at, TIMESTAMP('2026-05-01 16:20:00') completed_at, '演示生产批次：蛋黄鲜肉粽' remark UNION ALL
  SELECT 'PO-DEMO-HSLXR-20260502', 'FP-ZONG-HSLXR-188-20260501', 'FP-ZONG-HSLXR-188', 300, 270, 260, 10, 'BOXING', 'COMPLETED', DATE('2026-05-02'), TIMESTAMP('2026-05-02 08:40:00'), TIMESTAMP('2026-05-02 15:50:00'), '演示生产批次：黑松露鲜肉粽' UNION ALL
  SELECT 'PO-DEMO-SJCPDS-20260503', 'FP-ZONG-SJCPDS-160-20260501', 'FP-ZONG-SJCPDS-160', 420, 390, 380, 10, 'BOXING', 'COMPLETED', DATE('2026-05-03'), TIMESTAMP('2026-05-03 08:20:00'), TIMESTAMP('2026-05-03 16:10:00'), '演示生产批次：水晶陈皮豆沙粽'
) x
JOIN product p ON p.code = x.product_code
ON DUPLICATE KEY UPDATE
  planned_quantity = VALUES(planned_quantity),
  completed_quantity = VALUES(completed_quantity),
  inbound_quantity = VALUES(inbound_quantity),
  loss_quantity = VALUES(loss_quantity),
  status = VALUES(status),
  remark = VALUES(remark);

INSERT INTO sales_channel_config (code, name, source_type, enabled, sort_order, remark)
VALUES
  ('OFFLINE', '线下门店', 'MANUAL', 1, 10, '线下门店和熟客订单'),
  ('WECHAT_GROUP', '微信群团购', 'TEXT', 1, 20, '微信群文本订单'),
  ('DOUYIN', '抖音小店', 'EXCEL', 1, 30, '抖音平台订单'),
  ('PINDUODUO', '拼多多', 'EXCEL', 1, 40, '拼多多平台订单')
ON DUPLICATE KEY UPDATE name = VALUES(name), source_type = VALUES(source_type), enabled = VALUES(enabled), sort_order = VALUES(sort_order), remark = VALUES(remark);

INSERT INTO sales_goods (code, name, category, specification, unit, default_price, enabled, remark)
VALUES
  ('SG-DHXR-1', '蛋黄鲜肉粽 单只装', '粽子', '188g*1', '个', 12.80, 1, '单只零售'),
  ('SG-HSLXR-1', '黑松露鲜肉粽 单只装', '粽子', '188g*1', '个', 19.80, 1, '高端口味单只零售'),
  ('SG-SJCPDS-1', '水晶陈皮豆沙粽 单只装', '粽子', '160g*1', '个', 11.80, 1, '甜口单只零售'),
  ('SG-DHXR-6-GIFT', '蛋黄鲜肉粽 6只礼盒', '礼盒', '188g*6', '盒', 88.00, 1, '经典口味礼盒'),
  ('SG-HSLXR-6-GIFT', '黑松露鲜肉粽 6只礼盒', '礼盒', '188g*6', '盒', 128.00, 1, '高端口味礼盒'),
  ('SG-MIX-10-GIFT', '莞瑞臻味混合粽 10只礼盒', '礼盒', '10只装', '盒', 138.00, 1, '蛋黄鲜肉4只、黑松露3只、水晶陈皮豆沙3只')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  specification = VALUES(specification),
  unit = VALUES(unit),
  default_price = VALUES(default_price),
  enabled = VALUES(enabled),
  remark = VALUES(remark);

INSERT INTO sales_goods_component (sales_goods_id, product_id, quantity_per_unit, loss_rate, remark)
SELECT sg.id, p.id, x.qty, x.loss_rate, x.remark
FROM (
  SELECT 'SG-DHXR-1' sg, 'FP-ZONG-DHXR-188' p, 1.0000 qty, 0.0000 loss_rate, '单只扣减' remark UNION ALL
  SELECT 'SG-HSLXR-1', 'FP-ZONG-HSLXR-188', 1.0000, 0.0000, '单只扣减' UNION ALL
  SELECT 'SG-SJCPDS-1', 'FP-ZONG-SJCPDS-160', 1.0000, 0.0000, '单只扣减' UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 'FP-ZONG-DHXR-188', 6.0000, 0.0000, '礼盒内粽子' UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 'PK-GIFT-6', 1.0000, 0.0000, '礼盒包装' UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 'PK-ICE-BAG', 1.0000, 0.0000, '冷链冰袋' UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 'FP-ZONG-HSLXR-188', 6.0000, 0.0000, '礼盒内粽子' UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 'PK-GIFT-6', 1.0000, 0.0000, '礼盒包装' UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 'PK-ICE-BAG', 1.0000, 0.0000, '冷链冰袋' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'FP-ZONG-DHXR-188', 4.0000, 0.0000, '混合礼盒蛋黄鲜肉' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'FP-ZONG-HSLXR-188', 3.0000, 0.0000, '混合礼盒黑松露鲜肉' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'FP-ZONG-SJCPDS-160', 3.0000, 0.0000, '混合礼盒水晶陈皮豆沙' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'PK-GIFT-10', 1.0000, 0.0000, '10只礼盒包装' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'PK-ICE-BAG', 1.0000, 0.0000, '冷链冰袋'
) x
JOIN sales_goods sg ON sg.code = x.sg
JOIN product p ON p.code = x.p
ON DUPLICATE KEY UPDATE quantity_per_unit = VALUES(quantity_per_unit), loss_rate = VALUES(loss_rate), remark = VALUES(remark);

INSERT INTO sales_goods_channel_price (sales_goods_id, channel_id, price, enabled, remark)
SELECT sg.id, sc.id,
       CASE sc.code
         WHEN 'OFFLINE' THEN x.offline_price
         WHEN 'WECHAT_GROUP' THEN x.wechat_price
         WHEN 'DOUYIN' THEN x.douyin_price
         WHEN 'PINDUODUO' THEN x.pdd_price
       END,
       1,
       CONCAT(sc.name, '演示价格')
FROM (
  SELECT 'SG-DHXR-1' sg, 12.80 offline_price, 11.80 wechat_price, 13.80 douyin_price, 10.90 pdd_price UNION ALL
  SELECT 'SG-HSLXR-1', 19.80, 18.80, 21.80, 17.90 UNION ALL
  SELECT 'SG-SJCPDS-1', 11.80, 10.80, 12.80, 9.90 UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 88.00, 82.00, 92.00, 79.00 UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 128.00, 118.00, 138.00, 112.00 UNION ALL
  SELECT 'SG-MIX-10-GIFT', 138.00, 128.00, 148.00, 122.00
) x
JOIN sales_goods sg ON sg.code = x.sg
JOIN sales_channel_config sc ON sc.code IN ('OFFLINE', 'WECHAT_GROUP', 'DOUYIN', 'PINDUODUO')
ON DUPLICATE KEY UPDATE price = VALUES(price), enabled = VALUES(enabled), remark = VALUES(remark);

INSERT INTO sales_goods_match_rule (
  channel_id, external_product_name, external_spec_name, external_sku_code, sales_goods_id,
  quantity_multiplier, default_unit_price, match_type, enabled, priority, remark
)
SELECT sc.id, x.external_name, x.external_spec, x.external_sku, sg.id, x.multiplier, x.default_price, x.match_type, 1, x.priority, x.remark
FROM (
  SELECT 'WECHAT_GROUP' channel_code, '蛋黄鲜肉粽' external_name, '单只' external_spec, NULL external_sku, 'SG-DHXR-1' sg, 1.0000 multiplier, 11.80 default_price, 'CONTAINS' match_type, 10 priority, '微信群文本匹配：蛋黄鲜肉粽' remark UNION ALL
  SELECT 'WECHAT_GROUP', '黑松露鲜肉粽', '单只', NULL, 'SG-HSLXR-1', 1.0000, 18.80, 'CONTAINS', 10, '微信群文本匹配：黑松露鲜肉粽' UNION ALL
  SELECT 'WECHAT_GROUP', '水晶陈皮豆沙粽', '单只', NULL, 'SG-SJCPDS-1', 1.0000, 10.80, 'CONTAINS', 10, '微信群文本匹配：水晶陈皮豆沙粽' UNION ALL
  SELECT 'DOUYIN', '莞瑞蛋黄鲜肉粽6只礼盒', '188g*6', 'DY-DHXR-6', 'SG-DHXR-6-GIFT', 1.0000, 92.00, 'EXACT', 20, '抖音SKU匹配' UNION ALL
  SELECT 'DOUYIN', '莞瑞黑松露鲜肉粽6只礼盒', '188g*6', 'DY-HSLXR-6', 'SG-HSLXR-6-GIFT', 1.0000, 138.00, 'EXACT', 20, '抖音SKU匹配' UNION ALL
  SELECT 'DOUYIN', '莞瑞臻味混合粽10只礼盒', '10只装', 'DY-MIX-10', 'SG-MIX-10-GIFT', 1.0000, 148.00, 'EXACT', 20, '抖音SKU匹配' UNION ALL
  SELECT 'PINDUODUO', '蛋黄肉粽礼盒', '6只装', 'PDD-DHXR-6', 'SG-DHXR-6-GIFT', 1.0000, 79.00, 'CONTAINS', 30, '拼多多标题匹配' UNION ALL
  SELECT 'PINDUODUO', '混合粽礼盒', '10枚', 'PDD-MIX-10', 'SG-MIX-10-GIFT', 1.0000, 122.00, 'CONTAINS', 30, '拼多多标题匹配'
) x
JOIN sales_channel_config sc ON sc.code = x.channel_code
JOIN sales_goods sg ON sg.code = x.sg;

SET FOREIGN_KEY_CHECKS = 1;
