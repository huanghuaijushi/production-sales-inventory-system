-- Demo data for local/testing databases.
-- Apply after Flyway V1, for example:
-- mysql -h127.0.0.1 -uroot -p123456 production_sales_inventory < production-sales-inventory-api/src/main/resources/db/seed/demo_data.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO sys_user (id, username, password_hash, nickname, role, status)
VALUES (1, 'admin', '$2a$10$8FqKkXoZ9Y1xZr9DEMOHASHFORLOCALONLY', '系统管理员', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), status = VALUES(status);

INSERT INTO product_category (name, type, sort_order, enabled, remark)
VALUES
  ('粽子', 'FINISHED_PRODUCT', 10, 1, '粽子成品'),
  ('半成品', 'FINISHED_PRODUCT', 20, 1, '生产过程半成品'),
  ('原料', 'RAW_MATERIAL', 10, 1, '生产主辅料'),
  ('包装', 'PACKAGING_MATERIAL', 20, 1, '包装耗材')
ON DUPLICATE KEY UPDATE
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled),
  remark = VALUES(remark);

INSERT INTO product (code, name, type, category_id, specification, unit, cost_price, alert_quantity, shelf_life_days, description, enabled)
SELECT x.code, x.name, x.type, pc.id, x.specification, x.unit, x.cost_price, x.alert_quantity, x.shelf_life_days, x.description, x.enabled
FROM (
  SELECT 'FP-ZONG-DHXR-188' code, '蛋黄鲜肉粽' name, 'FINISHED_PRODUCT' type, '粽子' category_name, '188g/个' specification, '个' unit, 4.80 cost_price, 120 alert_quantity, 180 shelf_life_days, '经典蛋黄鲜肉粽成品库存' description, 1 enabled UNION ALL
  SELECT 'FP-ZONG-HSLXR-188', '黑松露鲜肉粽', 'FINISHED_PRODUCT', '粽子', '188g/个', '个', 7.60, 80, 180, '黑松露鲜肉粽成品库存', 1 UNION ALL
  SELECT 'FP-ZONG-SJCPDS-160', '水晶陈皮豆沙粽', 'FINISHED_PRODUCT', '粽子', '160g/个', '个', 4.20, 100, 180, '水晶陈皮豆沙粽成品库存', 1 UNION ALL
  SELECT 'RM-RICE-NM-25KG', '圆粒糯米', 'RAW_MATERIAL', '原料', '25kg/袋', 'kg', 7.20, 300, 365, '粽子主料糯米', 1 UNION ALL
  SELECT 'RM-PORK-WH-1KG', '五花鲜肉', 'RAW_MATERIAL', '原料', '1kg/袋', 'kg', 38.00, 80, 7, '鲜肉粽用五花肉', 1 UNION ALL
  SELECT 'RM-EGG-YOLK-20G', '咸蛋黄', 'RAW_MATERIAL', '原料', '20g/粒', '粒', 1.35, 300, 180, '蛋黄鲜肉粽用咸蛋黄', 1 UNION ALL
  SELECT 'RM-TRUFFLE-100G', '黑松露酱', 'RAW_MATERIAL', '原料', '100g/瓶', 'g', 0.42, 800, 365, '黑松露鲜肉粽风味料', 1 UNION ALL
  SELECT 'RM-DOUSHA-5KG', '陈皮豆沙馅', 'RAW_MATERIAL', '原料', '5kg/袋', 'kg', 18.00, 120, 180, '水晶陈皮豆沙粽馅料', 1 UNION ALL
  SELECT 'RM-CRYSTAL-POWDER', '水晶粉', 'RAW_MATERIAL', '原料', '10kg/袋', 'kg', 12.50, 100, 365, '水晶粽外皮原料', 1 UNION ALL
  SELECT 'RM-ZONG-LEAF', '箬竹粽叶', 'RAW_MATERIAL', '原料', '500片/包', '片', 0.16, 1000, 365, '粽叶', 1 UNION ALL
  SELECT 'RM-COTTON-THREAD', '棉线', 'PACKAGING_MATERIAL', '包装', '100m/卷', '米', 0.03, 500, 3650, '捆扎棉线', 1 UNION ALL
  SELECT 'PK-GIFT-6', '6只装礼盒', 'PACKAGING_MATERIAL', '包装', '6只/盒', '个', 5.20, 100, 3650, '粽子礼盒包装', 1 UNION ALL
  SELECT 'PK-GIFT-10', '10只装礼盒', 'PACKAGING_MATERIAL', '包装', '10只/盒', '个', 7.80, 80, 3650, '家庭装礼盒包装', 1 UNION ALL
  SELECT 'PK-ICE-BAG', '保温冰袋', 'PACKAGING_MATERIAL', '包装', '个', '个', 1.10, 200, 3650, '冷链保温冰袋', 1
) x
LEFT JOIN product_category pc ON pc.name = x.category_name AND pc.type = x.type
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  type = VALUES(type),
  category_id = VALUES(category_id),
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

INSERT INTO production_route_step (product_id, step_code, step_name, sort_order, allow_loss, enabled)
SELECT p.id, x.step_code, x.step_name, x.sort_order, x.allow_loss, 1
FROM product p
JOIN (
  SELECT 'PREPARATION' step_code, '备料' step_name, 10 sort_order, 1 allow_loss UNION ALL
  SELECT 'WRAPPING', '包制', 20, 1 UNION ALL
  SELECT 'COOKING', '蒸煮', 30, 1 UNION ALL
  SELECT 'PACKAGING', '包装', 40, 1 UNION ALL
  SELECT 'STERILIZATION', '杀菌', 50, 1 UNION ALL
  SELECT 'BOXING', '装箱', 60, 1
) x
WHERE p.code IN ('FP-ZONG-DHXR-188','FP-ZONG-HSLXR-188','FP-ZONG-SJCPDS-160')
ON DUPLICATE KEY UPDATE
  step_name = VALUES(step_name),
  sort_order = VALUES(sort_order),
  allow_loss = VALUES(allow_loss),
  enabled = VALUES(enabled);

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

INSERT INTO production_order_step (
  production_order_id, step_code, step_name, sort_order, allow_loss, completed_quantity, loss_quantity
)
SELECT po.id, prs.step_code, prs.step_name, prs.sort_order, prs.allow_loss, po.completed_quantity, 0
FROM production_order po
JOIN production_route_step prs ON prs.product_id = po.product_id
WHERE po.order_no IN ('PO-DEMO-DHXR-20260501','PO-DEMO-HSLXR-20260502','PO-DEMO-SJCPDS-20260503')
ON DUPLICATE KEY UPDATE
  step_name = VALUES(step_name),
  sort_order = VALUES(sort_order),
  allow_loss = VALUES(allow_loss),
  completed_quantity = VALUES(completed_quantity);

INSERT INTO sales_channel_config (code, name, source_type, enabled, sort_order, remark)
VALUES
  ('OFFLINE', '线下门店', 'MANUAL', 1, 10, '线下门店和熟客订单'),
  ('WECHAT_GROUP', '微信群团购', 'TEXT', 1, 20, '微信群文本订单'),
  ('DOUYIN', '抖音小店', 'EXCEL', 1, 30, '抖音平台订单'),
  ('PINDUODUO', '拼多多', 'EXCEL', 1, 40, '拼多多平台订单')
ON DUPLICATE KEY UPDATE name = VALUES(name), source_type = VALUES(source_type), enabled = VALUES(enabled), sort_order = VALUES(sort_order), remark = VALUES(remark);

INSERT INTO sales_goods (code, name, category, specification, unit, default_price, is_base, enabled, remark)
VALUES
  ('SG-DHXR-1', '蛋黄鲜肉粽 单只装', '粽子', '188g*1', '个', 12.80, 1, 1, '单只零售'),
  ('SG-HSLXR-1', '黑松露鲜肉粽 单只装', '粽子', '188g*1', '个', 19.80, 1, 1, '高端口味单只零售'),
  ('SG-SJCPDS-1', '水晶陈皮豆沙粽 单只装', '粽子', '160g*1', '个', 11.80, 1, 1, '甜口单只零售'),
  ('SG-DHXR-6-GIFT', '蛋黄鲜肉粽 6只礼盒', '礼盒', '188g*6', '盒', 88.00, 1, 1, '经典口味礼盒'),
  ('SG-HSLXR-6-GIFT', '黑松露鲜肉粽 6只礼盒', '礼盒', '188g*6', '盒', 128.00, 1, 1, '高端口味礼盒'),
  ('SG-MIX-10-GIFT', '莞瑞臻味混合粽 10只礼盒', '礼盒', '10只装', '盒', 138.00, 1, 1, '蛋黄鲜肉4只、黑松露3只、水晶陈皮豆沙3只')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  category = VALUES(category),
  specification = VALUES(specification),
  unit = VALUES(unit),
  default_price = VALUES(default_price),
  is_base = VALUES(is_base),
  enabled = VALUES(enabled),
  remark = VALUES(remark);

INSERT INTO sales_sku (sales_goods_id, code, name, spec_name, unit, per_sku_price, enabled, remark)
SELECT sg.id, CONCAT(sg.code, '-1'), sg.name, sg.specification, sg.unit, sg.default_price, 1, sg.remark
FROM sales_goods sg
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  spec_name = VALUES(spec_name),
  unit = VALUES(unit),
  per_sku_price = VALUES(per_sku_price),
  enabled = VALUES(enabled),
  remark = VALUES(remark);

INSERT INTO sales_sku_component (sales_sku_id, product_id, quantity, remark)
SELECT sku.id, p.id, x.qty, x.remark
FROM (
  SELECT 'SG-DHXR-1' sg, 'FP-ZONG-DHXR-188' p, 1.0000 qty, '单只扣减' remark UNION ALL
  SELECT 'SG-HSLXR-1', 'FP-ZONG-HSLXR-188', 1.0000, '单只扣减' UNION ALL
  SELECT 'SG-SJCPDS-1', 'FP-ZONG-SJCPDS-160', 1.0000, '单只扣减' UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 'FP-ZONG-DHXR-188', 6.0000, '礼盒内粽子' UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 'PK-GIFT-6', 1.0000, '礼盒包装' UNION ALL
  SELECT 'SG-DHXR-6-GIFT', 'PK-ICE-BAG', 1.0000, '冷链冰袋' UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 'FP-ZONG-HSLXR-188', 6.0000, '礼盒内粽子' UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 'PK-GIFT-6', 1.0000, '礼盒包装' UNION ALL
  SELECT 'SG-HSLXR-6-GIFT', 'PK-ICE-BAG', 1.0000, '冷链冰袋' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'FP-ZONG-DHXR-188', 4.0000, '混合礼盒蛋黄鲜肉' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'FP-ZONG-HSLXR-188', 3.0000, '混合礼盒黑松露鲜肉' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'FP-ZONG-SJCPDS-160', 3.0000, '混合礼盒水晶陈皮豆沙' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'PK-GIFT-10', 1.0000, '10只礼盒包装' UNION ALL
  SELECT 'SG-MIX-10-GIFT', 'PK-ICE-BAG', 1.0000, '冷链冰袋'
) x
JOIN sales_goods sg ON sg.code = x.sg
JOIN sales_sku sku ON sku.sales_goods_id = sg.id
JOIN product p ON p.code = x.p
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), remark = VALUES(remark);

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
  channel_id, external_product_name, external_spec_name, external_sku_code, sales_sku_id,
  match_type, enabled, priority, remark
)
SELECT sc.id, x.external_name, x.external_spec, x.external_sku, sku.id, x.match_type, 1, x.priority, x.remark
FROM (
  SELECT 'WECHAT_GROUP' channel_code, '蛋黄鲜肉粽' external_name, '单只' external_spec, NULL external_sku, 'SG-DHXR-1' sg, 'CONTAINS' match_type, 10 priority, '微信群文本匹配：蛋黄鲜肉粽' remark UNION ALL
  SELECT 'WECHAT_GROUP', '黑松露鲜肉粽', '单只', NULL, 'SG-HSLXR-1', 'CONTAINS', 10, '微信群文本匹配：黑松露鲜肉粽' UNION ALL
  SELECT 'WECHAT_GROUP', '水晶陈皮豆沙粽', '单只', NULL, 'SG-SJCPDS-1', 'CONTAINS', 10, '微信群文本匹配：水晶陈皮豆沙粽' UNION ALL
  SELECT 'DOUYIN', '莞瑞蛋黄鲜肉粽6只礼盒', '188g*6', 'DY-DHXR-6', 'SG-DHXR-6-GIFT', 'EXACT', 20, '抖音SKU匹配' UNION ALL
  SELECT 'DOUYIN', '莞瑞黑松露鲜肉粽6只礼盒', '188g*6', 'DY-HSLXR-6', 'SG-HSLXR-6-GIFT', 'EXACT', 20, '抖音SKU匹配' UNION ALL
  SELECT 'DOUYIN', '莞瑞臻味混合粽10只礼盒', '10只装', 'DY-MIX-10', 'SG-MIX-10-GIFT', 'EXACT', 20, '抖音SKU匹配' UNION ALL
  SELECT 'PINDUODUO', '蛋黄肉粽礼盒', '6只装', 'PDD-DHXR-6', 'SG-DHXR-6-GIFT', 'CONTAINS', 30, '拼多多标题匹配' UNION ALL
  SELECT 'PINDUODUO', '混合粽礼盒', '10枚', 'PDD-MIX-10', 'SG-MIX-10-GIFT', 'CONTAINS', 30, '拼多多标题匹配'
) x
JOIN sales_channel_config sc ON sc.code = x.channel_code
JOIN sales_goods sg ON sg.code = x.sg
JOIN sales_sku sku ON sku.sales_goods_id = sg.id;

-- ============================================================
-- 供应商
-- ============================================================
INSERT INTO supplier (name, contact_name, phone, address, remark, enabled)
VALUES
  ('合鑫米业', '张经理', '13800001001', '东莞市道滘镇南阁工业区', '糯米主供应商', 1),
  ('鲜达食品', '李经理', '13800001002', '东莞市厚街镇', '鲜肉供应商', 1),
  ('金品蛋业', '王经理', '13800001003', '东莞市沙田镇', '蛋品供应商', 1),
  ('绿源包材', '陈经理', '13800001004', '东莞市万江区', '包装耗材供应商', 1),
  ('松露生物科技', '赵总', '13800001005', '深圳市南山区', '黑松露酱供应商', 1),
  ('好味食品原料', '刘经理', '13800001006', '东莞市莞城区', '馅料粉料供应商', 1)
ON DUPLICATE KEY UPDATE
  contact_name = VALUES(contact_name),
  phone = VALUES(phone),
  address = VALUES(address),
  remark = VALUES(remark),
  enabled = VALUES(enabled);

-- ============================================================
-- 供货规则
-- ============================================================
INSERT INTO supplier_material (supplier_id, product_id, default_unit_price, min_order_quantity, order_multiple, lead_time_days, preferred, remark)
SELECT sup.id, p.id, x.unit_price, x.min_qty, x.multiple, x.lead_days, x.preferred, x.remark
FROM (
  SELECT '合鑫米业' sup_name, 'RM-RICE-NM-25KG' p_code, 7.20 unit_price, 20 min_qty, 10 multiple, 3 lead_days, 1 preferred, '优先供应商' remark UNION ALL
  SELECT '鲜达食品', 'RM-PORK-WH-1KG', 38.00, 10, 5, 1, 1, '优先供应商' UNION ALL
  SELECT '金品蛋业', 'RM-EGG-YOLK-20G', 1.35, 100, 50, 2, 1, '优先供应商' UNION ALL
  SELECT '松露生物科技', 'RM-TRUFFLE-100G', 0.42, 500, 100, 3, 1, '黑松露酱唯一供应商' UNION ALL
  SELECT '好味食品原料', 'RM-DOUSHA-5KG', 18.00, 20, 10, 3, 1, '馅料供应商' UNION ALL
  SELECT '好味食品原料', 'RM-CRYSTAL-POWDER', 12.50, 20, 10, 3, 1, '粉料供应商' UNION ALL
  SELECT '绿源包材', 'RM-ZONG-LEAF', 0.16, 500, 200, 2, 1, '粽叶供应商' UNION ALL
  SELECT '绿源包材', 'RM-COTTON-THREAD', 0.03, 100, 50, 2, 1, '棉线供应商' UNION ALL
  SELECT '绿源包材', 'PK-GIFT-6', 5.20, 50, 10, 5, 1, '6只装礼盒供应商' UNION ALL
  SELECT '绿源包材', 'PK-GIFT-10', 7.80, 50, 10, 5, 1, '10只装礼盒供应商' UNION ALL
  SELECT '绿源包材', 'PK-ICE-BAG', 1.10, 100, 50, 3, 1, '冰袋供应商'
) x
JOIN supplier sup ON sup.name = x.sup_name
JOIN product p ON p.code = x.p_code
ON DUPLICATE KEY UPDATE
  default_unit_price = VALUES(default_unit_price),
  min_order_quantity = VALUES(min_order_quantity),
  order_multiple = VALUES(order_multiple),
  lead_time_days = VALUES(lead_time_days),
  preferred = VALUES(preferred),
  remark = VALUES(remark);

-- ============================================================
-- 生产用料计划（根据BOM配方 × 计划生产数量计算）
-- ============================================================
INSERT INTO production_material_plan (production_order_id, material_product_id, required_quantity, issued_quantity)
SELECT po.id, rm.id, ROUND(x.planned_qty * bi.quantity_per_unit * (1 + bi.loss_rate)), ROUND(x.planned_qty * bi.quantity_per_unit * (1 + bi.loss_rate))
FROM (
  SELECT 'PO-DEMO-DHXR-20260501' order_no, 'FP-ZONG-DHXR-188' fp, 600 planned_qty UNION ALL
  SELECT 'PO-DEMO-HSLXR-20260502', 'FP-ZONG-HSLXR-188', 300 UNION ALL
  SELECT 'PO-DEMO-SJCPDS-20260503', 'FP-ZONG-SJCPDS-160', 420
) x
JOIN production_order po ON po.order_no = x.order_no
JOIN product fp ON fp.code = x.fp
JOIN bom_item bi ON bi.finished_product_id = fp.id
JOIN product rm ON rm.id = bi.material_product_id
ON DUPLICATE KEY UPDATE
  required_quantity = VALUES(required_quantity),
  issued_quantity = VALUES(issued_quantity);

-- ============================================================
-- 生产领料记录
-- ============================================================
INSERT INTO production_material_issue (
  production_order_id, material_plan_id, material_product_id, stock_batch_id, batch_no,
  issued_quantity, operator_id, operator_name, remark
)
SELECT po.id, pmp.id, pmp.material_product_id, sb.id, sb.batch_no,
       pmp.issued_quantity, 1, 'admin', CONCAT('演示领料：', rm.name)
FROM production_order po
JOIN production_material_plan pmp ON pmp.production_order_id = po.id
JOIN product rm ON rm.id = pmp.material_product_id
JOIN stock_batch sb ON sb.product_id = pmp.material_product_id AND sb.batch_no = CONCAT(rm.code, '-20260501')
WHERE po.order_no IN ('PO-DEMO-DHXR-20260501','PO-DEMO-HSLXR-20260502','PO-DEMO-SJCPDS-20260503');

-- ============================================================
-- 工序执行记录
-- ============================================================
INSERT INTO production_step_record (
  production_order_id, step_type, step_name, completed_quantity, loss_quantity,
  loss_reason, operator_id, operator_name
)
SELECT po.id, pos.step_code, pos.step_name, pos.completed_quantity, pos.loss_quantity,
       CASE WHEN pos.loss_quantity > 0 THEN CONCAT('演示损耗-', pos.step_name) ELSE NULL END,
       1, 'admin'
FROM production_order po
JOIN production_order_step pos ON pos.production_order_id = po.id
WHERE po.order_no IN ('PO-DEMO-DHXR-20260501','PO-DEMO-HSLXR-20260502','PO-DEMO-SJCPDS-20260503')
ON DUPLICATE KEY UPDATE
  completed_quantity = VALUES(completed_quantity),
  loss_quantity = VALUES(loss_quantity),
  loss_reason = VALUES(loss_reason);

-- ============================================================
-- 演示采购单
-- ============================================================
INSERT INTO purchase_order (order_no, supplier_id, status, expected_arrival_date, total_amount, operator_id, operator_name, remark, inbound_at)
SELECT x.order_no, sup.id, x.status, x.arrival_date, x.total_amount, 1, 'admin', x.remark, x.inbound_at
FROM (
  SELECT 'PUR-DEMO-20260505' order_no, '合鑫米业' sup_name, 'INBOUNDED' status, DATE('2026-05-05') arrival_date, 2880.00 total_amount, '演示采购：糯米补货80袋' remark, TIMESTAMP('2026-05-05 09:00:00') inbound_at UNION ALL
  SELECT 'PUR-DEMO-20260506', '鲜达食品', 'INBOUNDED', DATE('2026-05-06'), 3800.00, '演示采购：五花肉100kg', TIMESTAMP('2026-05-06 09:30:00') UNION ALL
  SELECT 'PUR-DEMO-20260507', '绿源包材', 'INBOUNDED', DATE('2026-05-07'), 1580.00, '演示采购：礼盒冰袋补货', TIMESTAMP('2026-05-07 10:00:00') UNION ALL
  SELECT 'PUR-DEMO-20260510', '金品蛋业', 'PENDING_INBOUND', DATE('2026-05-12'), 2700.00, '演示采购：咸蛋黄补货2000粒，待入库', NULL
) x
JOIN supplier sup ON sup.name = x.sup_name
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  total_amount = VALUES(total_amount),
  remark = VALUES(remark);

INSERT INTO purchase_order_item (order_id, product_id, product_code, product_name, product_specification, product_unit, quantity, unit_price, amount)
SELECT po.id, p.id, p.code, p.name, p.specification, p.unit, x.quantity, x.unit_price, x.quantity * x.unit_price
FROM (
  SELECT 'PUR-DEMO-20260505' order_no, 'RM-RICE-NM-25KG' p_code, 2000 quantity, 7.20 unit_price UNION ALL
  SELECT 'PUR-DEMO-20260506', 'RM-PORK-WH-1KG', 100, 38.00 UNION ALL
  SELECT 'PUR-DEMO-20260507', 'PK-GIFT-6', 80, 5.20 UNION ALL
  SELECT 'PUR-DEMO-20260507', 'PK-GIFT-10', 60, 7.80 UNION ALL
  SELECT 'PUR-DEMO-20260507', 'PK-ICE-BAG', 300, 1.10 UNION ALL
  SELECT 'PUR-DEMO-20260510', 'RM-EGG-YOLK-20G', 2000, 1.35
) x
JOIN purchase_order po ON po.order_no = x.order_no
JOIN product p ON p.code = x.p_code
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  unit_price = VALUES(unit_price),
  amount = VALUES(amount);

-- ============================================================
-- 演示销售订单
-- ============================================================
INSERT INTO sales_order (order_no, channel, channel_id, customer_name, customer_phone, customer_address, total_amount, status, order_date, operator_id, operator_name, remark, source_type)
SELECT x.order_no, sc.code, sc.id, x.customer_name, x.customer_phone, x.customer_address, x.total_amount, x.status, x.order_date, 1, 'admin', x.remark, sc.source_type
FROM (
  SELECT 'SO-DEMO-OFFLINE-001' order_no, 'OFFLINE' channel_code, '陈先生' customer_name, '13900001001' customer_phone, '东莞市南城区' customer_address, 304.00 total_amount, 'COMPLETED' status, TIMESTAMP('2026-05-08 10:30:00') order_date, '线下熟客：礼盒粽子' remark UNION ALL
  SELECT 'SO-DEMO-WECHAT-001', 'WECHAT_GROUP', '刘小姐', '13900001002', '东莞市东城区', 172.00, 'SHIPPED', TIMESTAMP('2026-05-09 14:20:00'), '微信群团购：散装零售' UNION ALL
  SELECT 'SO-DEMO-DOUYIN-001', 'DOUYIN', '赵女士', '13900001003', '广州市天河区', 148.00, 'PENDING', TIMESTAMP('2026-05-10 09:15:00'), '抖音小店：混合礼盒' UNION ALL
  SELECT 'SO-DEMO-OFFLINE-002', 'OFFLINE', '黄先生', '13900001004', '东莞市虎门镇', 88.00, 'PENDING', TIMESTAMP('2026-05-11 16:00:00'), '线下门店：蛋黄鲜肉礼盒' UNION ALL
  SELECT 'SO-DEMO-PDD-001', 'PINDUODUO', '孙女士', '13900001005', '深圳市宝安区', 122.00, 'PENDING', TIMESTAMP('2026-05-11 18:45:00'), '拼多多：混合粽礼盒' UNION ALL
  SELECT 'SO-DEMO-WECHAT-002', 'WECHAT_GROUP', '周小姐', '13900001006', '东莞市长安镇', 118.00, 'PENDING', TIMESTAMP('2026-05-12 08:30:00'), '微信群：黑松露礼盒'
) x
JOIN sales_channel_config sc ON sc.code = x.channel_code
ON DUPLICATE KEY UPDATE
  customer_name = VALUES(customer_name),
  total_amount = VALUES(total_amount),
  status = VALUES(status),
  remark = VALUES(remark);

INSERT INTO sales_order_item (order_id, sales_sku_id, sku_name, sales_goods_id, goods_code, goods_name, goods_specification, goods_unit, quantity, unit_price, subtotal)
SELECT so.id, sku.id, sku.name, sg.id, sg.code, sg.name, sg.specification, sg.unit, x.quantity, x.unit_price, x.quantity * x.unit_price
FROM (
  SELECT 'SO-DEMO-OFFLINE-001' order_no, 'SG-DHXR-6-GIFT' sg_code, 2 quantity, 88.00 unit_price UNION ALL
  SELECT 'SO-DEMO-OFFLINE-001', 'SG-HSLXR-6-GIFT', 1, 128.00 UNION ALL
  SELECT 'SO-DEMO-WECHAT-001', 'SG-DHXR-1', 10, 11.80 UNION ALL
  SELECT 'SO-DEMO-WECHAT-001', 'SG-SJCPDS-1', 5, 10.80 UNION ALL
  SELECT 'SO-DEMO-DOUYIN-001', 'SG-MIX-10-GIFT', 1, 148.00 UNION ALL
  SELECT 'SO-DEMO-OFFLINE-002', 'SG-DHXR-6-GIFT', 1, 88.00 UNION ALL
  SELECT 'SO-DEMO-PDD-001', 'SG-MIX-10-GIFT', 1, 122.00 UNION ALL
  SELECT 'SO-DEMO-WECHAT-002', 'SG-HSLXR-6-GIFT', 1, 118.00
) x
JOIN sales_order so ON so.order_no = x.order_no
JOIN sales_goods sg ON sg.code = x.sg_code
JOIN sales_sku sku ON sku.sales_goods_id = sg.id
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  unit_price = VALUES(unit_price),
  subtotal = VALUES(subtotal);

-- ============================================================
-- 订单导入批次
-- ============================================================
INSERT INTO order_import_batch (batch_no, channel_id, source_type, file_name, total_count, parsed_count, ready_count, converted_count, error_count, status, operator_id, operator_name)
SELECT x.batch_no, sc.id, x.source_type, x.file_name, x.total_count, x.parsed_count, x.ready_count, x.converted_count, 0, 'CONFIRMED', 1, 'admin'
FROM (
  SELECT 'BATCH-DEMO-20260510' batch_no, 'DOUYIN' channel_code, 'EXCEL' source_type, '抖音订单导入-20260510.xlsx' file_name, 1 total_count, 1 parsed_count, 1 ready_count, 1 converted_count UNION ALL
  SELECT 'BATCH-DEMO-20260511', 'PINDUODUO', 'EXCEL', '拼多多订单导入-20260511.xlsx', 1, 1, 1, 1
) x
JOIN sales_channel_config sc ON sc.code = x.channel_code
ON DUPLICATE KEY UPDATE
  total_count = VALUES(total_count),
  status = VALUES(status);

-- ============================================================
-- 外部原始订单
-- ============================================================
INSERT INTO external_order_raw (batch_id, channel_id, external_order_no, order_time, customer_name, customer_phone, customer_address, province, city, district, buyer_message, status, sales_order_id)
SELECT b.id, b.channel_id, x.ext_no, x.order_time, x.customer_name, x.customer_phone, x.customer_address, x.province, x.city, x.district, x.buyer_message, x.status, so.id
FROM (
  SELECT 'BATCH-DEMO-20260510' batch_no, 'DOUYIN-20260510001' ext_no, TIMESTAMP('2026-05-10 09:15:00') order_time, '赵女士' customer_name, '13900001003' customer_phone, '广州市天河区体育西路88号' customer_address, '广东' province, '广州' city, '天河' district, '请发顺丰，端午礼盒' buyer_message, 'CONVERTED' status, 'SO-DEMO-DOUYIN-001' order_no UNION ALL
  SELECT 'BATCH-DEMO-20260511', 'PDD-20260511001', TIMESTAMP('2026-05-11 18:45:00'), '孙女士', '13900001005', '深圳市宝安区西乡街道123号', '广东', '深圳', '宝安', '包装完好', 'CONVERTED', 'SO-DEMO-PDD-001'
) x
JOIN order_import_batch b ON b.batch_no = x.batch_no
LEFT JOIN sales_order so ON so.order_no = x.order_no
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  sales_order_id = VALUES(sales_order_id);

-- ============================================================
-- 外部原始订单明细
-- ============================================================
INSERT INTO external_order_item_raw (external_order_id, external_product_name, external_spec_name, external_quantity, external_unit_price, matched_sales_sku_id, matched_sku_name, mapping_id, sale_quantity, match_status, match_message)
SELECT eor.id, x.ext_name, x.ext_spec, x.ext_qty, x.ext_price, sku.id, sku.name, sgmr.id, x.ext_qty, 'MATCHED', '自动匹配成功'
FROM (
  SELECT 'DOUYIN-20260510001' ext_no, '黑松露蛋黄鲜肉粽混合礼盒' ext_name, '1×160克×10只' ext_spec, 1 ext_qty, 148.00 ext_price, 'SG-MIX-10-GIFT' sg_code UNION ALL
  SELECT 'PDD-20260511001', '蛋黄鲜肉端午大礼包', '10只装', 1, 122.00, 'SG-MIX-10-GIFT'
) x
JOIN external_order_raw eor ON eor.external_order_no = x.ext_no
JOIN sales_goods sg ON sg.code = x.sg_code
JOIN sales_sku sku ON sku.sales_goods_id = sg.id
JOIN sales_channel_config sc ON sc.id = eor.channel_id
JOIN sales_goods_match_rule sgmr ON sgmr.sales_sku_id = sku.id AND sgmr.channel_id = sc.id
ON DUPLICATE KEY UPDATE
  external_quantity = VALUES(external_quantity),
  external_unit_price = VALUES(external_unit_price),
  match_status = VALUES(match_status);

-- ============================================================
-- 库存盘点单（五一节后盘点）
-- ============================================================
INSERT INTO stock_check_order (check_no, status, operator_id, operator_name, remark, confirmed_at)
VALUES ('SC-DEMO-20260512', 'CONFIRMED', 1, 'admin', '五一节后盘点', NOW())
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  remark = VALUES(remark);

INSERT INTO stock_check_order_item (order_id, product_id, batch_id, batch_no, system_quantity, actual_quantity, difference_quantity, result_type, remark)
SELECT sco.id, p.id, sb.id, sb.batch_no, x.system_qty, x.actual_qty, x.actual_qty - x.system_qty, x.result_type, x.remark
FROM (
  SELECT 'RM-RICE-NM-25KG' p_code, 2500 system_qty, 2500 actual_qty, 'MATCH' result_type, '一致，无需调整' remark UNION ALL
  SELECT 'FP-ZONG-DHXR-188', 1200, 1198, 'LOSS', '盘亏2只，疑似出库未登记' UNION ALL
  SELECT 'RM-PORK-WH-1KG', 85, 84, 'LOSS', '盘亏1袋，正常库耗'
) x
JOIN stock_check_order sco ON sco.check_no = 'SC-DEMO-20260512'
JOIN product p ON p.code = x.p_code
JOIN stock_batch sb ON sb.product_id = p.id AND sb.batch_no = CONCAT(x.p_code, '-20260501')
ON DUPLICATE KEY UPDATE
  system_quantity = VALUES(system_quantity),
  actual_quantity = VALUES(actual_quantity),
  difference_quantity = VALUES(difference_quantity),
  result_type = VALUES(result_type),
  remark = VALUES(remark);

SET FOREIGN_KEY_CHECKS = 1;
