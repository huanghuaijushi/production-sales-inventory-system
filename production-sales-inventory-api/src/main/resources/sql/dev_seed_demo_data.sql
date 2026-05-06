USE production_sales_inventory;

INSERT INTO admin_user (username, password_hash, nickname, role, status, token_version)
VALUES ('admin', '$2a$10$Q3F1F6o0b8Yw3J7h1Qw5CegT9M4p3O4U9K0vYQFvK5wQhP8q6vL7e', '系统管理员', 'ADMIN', 'ACTIVE', 0)
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

INSERT INTO supplier (name, contact_name, phone, address, remark, enabled)
VALUES
    ('金禾粮油', '张经理', '13800000001', '浙江省宁波市', '主供应商', 1),
    ('海丰食品', '李经理', '13800000002', '浙江省嘉兴市', '稳定供货', 1),
    ('丰源包装', '王经理', '13800000003', '浙江省台州市', '包装耗材', 1)
ON DUPLICATE KEY UPDATE contact_name = VALUES(contact_name);

INSERT INTO product_category (name, type, sort_order, enabled, remark)
VALUES
    ('粽子', 'FINISHED_PRODUCT', 10, 1, '成品粽子'),
    ('半成品', 'FINISHED_PRODUCT', 20, 1, '生产过程半成品'),
    ('原料', 'RAW_MATERIAL', 10, 1, '生产原材料'),
    ('包装', 'RAW_MATERIAL', 20, 1, '包装与辅材')
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), enabled = VALUES(enabled), remark = VALUES(remark);

INSERT INTO product (code, name, type, category, specification, unit, cost_price, sale_price, alert_quantity, shelf_life_days, enabled)
VALUES
    ('FP001', '豆沙粽', 'FINISHED_PRODUCT', '粽子', '100g/个', '个', 2.50, 5.00, 500, 7, 1),
    ('FP002', '鲜肉粽', 'FINISHED_PRODUCT', '粽子', '120g/个', '个', 3.00, 6.00, 500, 7, 1),
    ('FP003', '蛋黄肉粽', 'FINISHED_PRODUCT', '粽子', '150g/个', '个', 4.00, 8.00, 300, 7, 1),
    ('RM001', '糯米', 'RAW_MATERIAL', '原料', '25kg/袋', '袋', 120.00, NULL, 10, NULL, 1),
    ('RM002', '豆沙馅', 'RAW_MATERIAL', '原料', '5kg/袋', '袋', 35.00, NULL, 5, 30, 1),
    ('RM003', '猪肉', 'RAW_MATERIAL', '原料', '1kg', 'kg', 25.00, NULL, 20, 3, 1),
    ('RM004', '粽叶', 'RAW_MATERIAL', '原料', '500片/包', '包', 15.00, NULL, 10, NULL, 1),
    ('RM005', '咸蛋黄', 'RAW_MATERIAL', '原料', '50个/盒', '盒', 40.00, NULL, 5, 15, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name), cost_price = VALUES(cost_price), sale_price = VALUES(sale_price), alert_quantity = VALUES(alert_quantity);

INSERT INTO purchase_order (order_no, supplier_id, status, expected_arrival_date, total_amount, operator_id, operator_name, remark, inbound_at)
VALUES
    ('PO202605050001', 1, 'INBOUNDED', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 18600.00, 1, '系统管理员', '糯米与豆沙馅补货', NOW()),
    ('PO202605050002', 2, 'INBOUNDED', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 11250.00, 1, '系统管理员', '猪肉与咸蛋黄补货', NOW()),
    ('PO202605050003', 3, 'PENDING_INBOUND', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 8400.00, 1, '系统管理员', '包装与标签', NULL)
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO purchase_order_item (order_id, product_id, product_code, product_name, product_specification, product_unit, quantity, unit_price, amount)
VALUES
    (1, 4, 'RM001', '糯米', '25kg/袋', '袋', 100, 120.00, 12000.00),
    (1, 5, 'RM002', '豆沙馅', '5kg/袋', '袋', 40, 35.00, 1400.00),
    (1, 7, 'RM004', '粽叶', '500片/包', '包', 35, 15.00, 525.00),
    (2, 6, 'RM003', '猪肉', '1kg', 'kg', 300, 25.00, 7500.00),
    (2, 8, 'RM005', '咸蛋黄', '50个/盒', '盒', 100, 40.00, 4000.00),
    (3, 7, 'RM004', '粽叶', '500片/包', '包', 140, 15.00, 2100.00)
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

INSERT INTO stock (product_id, quantity, locked_quantity, available_quantity)
VALUES
    (1, 1650, 120, 1530),
    (2, 980, 80, 900),
    (3, 420, 40, 380),
    (4, 128, 0, 128),
    (5, 42, 0, 42),
    (6, 28, 0, 28),
    (7, 120, 0, 120),
    (8, 36, 0, 36)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), locked_quantity = VALUES(locked_quantity), available_quantity = VALUES(available_quantity);

INSERT INTO stock_batch (product_id, batch_no, production_date, expiry_date, quantity, available_quantity, unit_cost, source_type, source_order_id, source_order_no, remark)
VALUES
    (4, 'RM001-20260501-A', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 60, 60, 120.00, 'PURCHASE_ORDER', 1, 'PO202605050001', '糯米批次A'),
    (4, 'RM001-20260503-B', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 22 DAY), 68, 68, 118.00, 'PURCHASE_ORDER', 1, 'PO202605050001', '糯米批次B'),
    (5, 'RM002-20260502-A', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 42, 42, 35.00, 'PURCHASE_ORDER', 1, 'PO202605050001', '豆沙馅批次'),
    (6, 'RM003-20260501-A', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 18, 18, 25.00, 'PURCHASE_ORDER', 2, 'PO202605050002', '猪肉批次A'),
    (6, 'RM003-20260503-B', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 10, 10, 26.00, 'PURCHASE_ORDER', 2, 'PO202605050002', '猪肉批次B'),
    (7, 'RM004-20260502-A', DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 120, 120, 15.00, 'PURCHASE_ORDER', 3, 'PO202605050003', '粽叶批次'),
    (8, 'RM005-20260502-A', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 36, 36, 40.00, 'PURCHASE_ORDER', 2, 'PO202605050002', '咸蛋黄批次'),
    (1, 'FP001-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 920, 920, 2.50, 'PRODUCTION_ORDER', 1, 'PRD202605050001', '豆沙粽成品批次'),
    (2, 'FP002-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 610, 610, 3.00, 'PRODUCTION_ORDER', 1, 'PRD202605050002', '鲜肉粽成品批次'),
    (3, 'FP003-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 280, 280, 4.00, 'PRODUCTION_ORDER', 1, 'PRD202605050003', '蛋黄肉粽成品批次')
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), available_quantity = VALUES(available_quantity);

INSERT INTO stock_record (record_no, product_id, type, sub_type, quantity, unit_price, amount, before_quantity, after_quantity, related_order_id, related_order_type, related_order_no, batch_id, batch_no, production_date, expiry_date, operator_id, operator_name, remark, created_at)
VALUES
    ('SR202605050001', 4, 'IN', 'PURCHASE', 128, 119.00, 15232.00, 0, 128, 1, 'PURCHASE_ORDER', 'PO202605050001', 1, 'RM001-20260501-A', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1, '系统管理员', '糯米采购入库', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('SR202605050002', 5, 'IN', 'PURCHASE', 42, 35.00, 1470.00, 0, 42, 1, 'PURCHASE_ORDER', 'PO202605050001', 3, 'RM002-20260502-A', DATE_SUB(CURDATE(), INTERVAL 3 DAY), DATE_ADD(CURDATE(), INTERVAL 18 DAY), 1, '系统管理员', '豆沙馅采购入库', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    ('SR202605050003', 6, 'IN', 'PURCHASE', 28, 25.00, 700.00, 0, 28, 2, 'PURCHASE_ORDER', 'PO202605050002', 4, 'RM003-20260501-A', DATE_SUB(CURDATE(), INTERVAL 4 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY), 1, '系统管理员', '猪肉采购入库', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    ('SR202605050004', 7, 'OUT', 'PRODUCTION_USAGE', -9, 15.00, 135.00, 129, 120, NULL, 'PRODUCTION_ORDER', 'PRD202605050001', 6, 'RM004-20260502-A', DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, 1, '系统管理员', '生产领料', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    ('SR202605050005', 1, 'IN', 'PRODUCTION', 920, 2.50, 2300.00, 0, 920, 1, 'PRODUCTION_ORDER', 'PRD202605050001', 8, 'FP001-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, '系统管理员', '豆沙粽生产入库', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    ('SR202605050006', 2, 'IN', 'PRODUCTION', 610, 3.00, 1830.00, 0, 610, 1, 'PRODUCTION_ORDER', 'PRD202605050002', 9, 'FP002-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, '系统管理员', '鲜肉粽生产入库', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    ('SR202605050007', 3, 'IN', 'PRODUCTION', 280, 4.00, 1120.00, 0, 280, 1, 'PRODUCTION_ORDER', 'PRD202605050003', 10, 'FP003-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, '系统管理员', '蛋黄肉粽生产入库', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    ('SR202605050008', 1, 'OUT', 'SALES', -120, 5.00, 600.00, 1650, 1530, NULL, 'SALES_ORDER', 'SO202605050001', 8, 'FP001-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, '系统管理员', '线上销售出库', NOW()),
    ('SR202605050009', 2, 'OUT', 'SALES', -80, 6.00, 480.00, 980, 900, NULL, 'SALES_ORDER', 'SO202605050002', 9, 'FP002-20260504-A', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, '系统管理员', '线下销售出库', NOW())
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit_price = VALUES(unit_price), amount = VALUES(amount);

INSERT INTO sales_order (order_no, channel, customer_name, customer_phone, customer_address, total_amount, status, order_date, ship_date, complete_date, operator_id, operator_name, remark)
VALUES
    ('SO202605050001', 'OFFLINE', '杭州禄记食品', '13800001001', '浙江省杭州市', 600.00, 'PENDING', NOW(), NULL, NULL, 1, '系统管理员', '门店补货'),
    ('SO202605050002', 'DOUYIN', '抖音团购客户', '13800001002', '浙江省宁波市', 480.00, 'SHIPPED', NOW(), NOW(), NULL, 1, '系统管理员', '直播销售')
ON DUPLICATE KEY UPDATE total_amount = VALUES(total_amount), status = VALUES(status);

INSERT INTO sales_order_item (order_id, product_id, product_code, product_name, product_specification, product_unit, product_category, quantity, unit_price, subtotal)
VALUES
    (1, 1, 'FP001', '豆沙粽', '100g/个', '个', '粽子', 120, 5.00, 600.00),
    (2, 2, 'FP002', '鲜肉粽', '120g/个', '个', '粽子', 80, 6.00, 480.00)
ON DUPLICATE KEY UPDATE subtotal = VALUES(subtotal);

INSERT INTO bom_item (finished_product_id, material_product_id, quantity_per_unit, loss_rate)
VALUES
    (1, 4, 0.2000, 0.0300),
    (1, 5, 0.0800, 0.0200),
    (1, 7, 0.0090, 0.0100),
    (2, 4, 0.1800, 0.0300),
    (2, 6, 0.1200, 0.0400),
    (2, 7, 0.0090, 0.0100),
    (3, 4, 0.2200, 0.0300),
    (3, 6, 0.1000, 0.0400),
    (3, 7, 0.0090, 0.0100),
    (3, 8, 0.1000, 0.0200)
ON DUPLICATE KEY UPDATE quantity_per_unit = VALUES(quantity_per_unit), loss_rate = VALUES(loss_rate);

INSERT INTO supplier_material (supplier_id, product_id, default_unit_price, min_order_quantity, order_multiple, lead_time_days, preferred, remark)
VALUES
    (1, 4, 120.00, 10, 5, 3, 1, '糯米主供应商'),
    (1, 5, 35.00, 5, 5, 2, 1, '豆沙馅主供应商'),
    (2, 6, 25.00, 20, 10, 1, 1, '猪肉主供应商'),
    (2, 8, 40.00, 10, 5, 2, 1, '咸蛋黄主供应商'),
    (3, 7, 15.00, 20, 10, 4, 1, '粽叶主供应商'),
    (3, 4, 122.00, 10, 5, 5, 0, '糯米备选供应商'),
    (1, 7, 16.00, 20, 10, 3, 0, '粽叶备选供应商')
ON DUPLICATE KEY UPDATE default_unit_price = VALUES(default_unit_price), min_order_quantity = VALUES(min_order_quantity), order_multiple = VALUES(order_multiple), lead_time_days = VALUES(lead_time_days), preferred = VALUES(preferred), remark = VALUES(remark);

INSERT INTO production_order (order_no, batch_no, product_id, planned_quantity, completed_quantity, inbound_quantity, loss_quantity, current_step, status, planned_date, started_at, completed_at, operator_id, operator_name, remark)
VALUES
    ('PRD202605050001', 'FP001-20260504-A', 1, 1000, 920, 920, 18, 'PACKAGING', 'COMPLETED', CURDATE(), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1, '系统管理员', '豆沙粽批次'),
    ('PRD202605050002', 'FP002-20260504-A', 2, 650, 610, 610, 12, 'PACKAGING', 'COMPLETED', CURDATE(), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 1, '系统管理员', '鲜肉粽批次'),
    ('PRD202605050003', 'FP003-20260504-A', 3, 320, 280, 280, 8, 'PACKAGING', 'WAIT_INBOUND', CURDATE(), DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 1, '系统管理员', '蛋黄肉粽批次')
ON DUPLICATE KEY UPDATE planned_quantity = VALUES(planned_quantity), completed_quantity = VALUES(completed_quantity), inbound_quantity = VALUES(inbound_quantity), loss_quantity = VALUES(loss_quantity), status = VALUES(status);

INSERT INTO production_material_plan (production_order_id, material_product_id, required_quantity, issued_quantity)
VALUES
    (1, 4, 200, 200),
    (1, 5, 80, 80),
    (2, 6, 120, 120),
    (2, 8, 40, 40),
    (3, 4, 80, 80),
    (3, 8, 30, 30)
ON DUPLICATE KEY UPDATE issued_quantity = VALUES(issued_quantity);

INSERT INTO production_material_issue (production_order_id, material_plan_id, material_product_id, stock_batch_id, batch_no, issued_quantity, stock_record_id, operator_id, operator_name, remark, created_at)
VALUES
    (1, 1, 4, 1, 'RM001-20260501-A', 120, '1', 1, '系统管理员', '豆沙粽工单领用糯米第一批', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 1, 4, 2, 'RM001-20260503-B', 80, '1', 1, '系统管理员', '豆沙粽工单领用糯米第二批', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 2, 5, 3, 'RM002-20260502-A', 80, '2', 1, '系统管理员', '豆沙粽工单领用豆沙馅', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (2, 3, 6, 4, 'RM003-20260501-A', 80, '3', 1, '系统管理员', '鲜肉粽工单领用猪肉第一批', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 3, 6, 5, 'RM003-20260503-B', 40, '3', 1, '系统管理员', '鲜肉粽工单领用猪肉第二批', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 4, 8, 7, 'RM005-20260502-A', 40, '7', 1, '系统管理员', '鲜肉粽工单领用咸蛋黄', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (3, 5, 4, 2, 'RM001-20260503-B', 80, '1', 1, '系统管理员', '蛋黄肉粽工单领用糯米', NOW()),
    (3, 6, 8, 7, 'RM005-20260502-A', 30, '7', 1, '系统管理员', '蛋黄肉粽工单领用咸蛋黄', NOW())
ON DUPLICATE KEY UPDATE issued_quantity = VALUES(issued_quantity), remark = VALUES(remark);

INSERT INTO production_step_record (production_order_id, step_type, completed_quantity, loss_quantity, loss_reason, operator_id, operator_name, created_at)
VALUES
    (1, 'WRAPPING', 920, 10, '少量破损', 1, '系统管理员', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 'PACKAGING', 920, 8, '包装轻微损耗', 1, '系统管理员', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 'WRAPPING', 610, 6, '手工包制损耗', 1, '系统管理员', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 'PACKAGING', 610, 6, '包装破损', 1, '系统管理员', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (3, 'WRAPPING', 280, 4, '少量破损', 1, '系统管理员', NOW()),
    (3, 'PACKAGING', 280, 4, '包装等待入库', 1, '系统管理员', NOW())
ON DUPLICATE KEY UPDATE loss_quantity = VALUES(loss_quantity);
