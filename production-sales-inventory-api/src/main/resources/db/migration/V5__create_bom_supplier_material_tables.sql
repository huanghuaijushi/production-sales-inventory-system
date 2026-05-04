CREATE TABLE `bom_item` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    finished_product_id BIGINT NOT NULL COMMENT '成品ID',
    material_product_id BIGINT NOT NULL COMMENT '原材料ID',
    quantity_per_unit DECIMAL(12, 4) NOT NULL COMMENT '生产1个成品需要的原材料数量',
    loss_rate DECIMAL(6, 4) NOT NULL DEFAULT 0 COMMENT '损耗率，如0.0300表示3%',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_bom_item_product_material UNIQUE (finished_product_id, material_product_id),
    CONSTRAINT fk_bom_item_finished_product FOREIGN KEY (finished_product_id) REFERENCES product (id) ON DELETE RESTRICT,
    CONSTRAINT fk_bom_item_material_product FOREIGN KEY (material_product_id) REFERENCES product (id) ON DELETE RESTRICT,
    CONSTRAINT ck_bom_item_quantity CHECK (quantity_per_unit > 0),
    CONSTRAINT ck_bom_item_loss_rate CHECK (loss_rate >= 0 AND loss_rate <= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成品配方明细表';

CREATE INDEX idx_bom_item_finished_product ON bom_item (finished_product_id);
CREATE INDEX idx_bom_item_material_product ON bom_item (material_product_id);

CREATE TABLE `supplier_material` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    product_id BIGINT NOT NULL COMMENT '原材料ID',
    default_unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '默认采购单价',
    min_order_quantity INT NOT NULL DEFAULT 1 COMMENT '最小起订量',
    order_multiple INT NOT NULL DEFAULT 1 COMMENT '采购倍数',
    lead_time_days INT NOT NULL DEFAULT 0 COMMENT '预计交期天数',
    preferred TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否优先供应商',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_supplier_material UNIQUE (supplier_id, product_id),
    CONSTRAINT fk_supplier_material_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id) ON DELETE RESTRICT,
    CONSTRAINT fk_supplier_material_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT,
    CONSTRAINT ck_supplier_material_price CHECK (default_unit_price >= 0),
    CONSTRAINT ck_supplier_material_min_order CHECK (min_order_quantity > 0),
    CONSTRAINT ck_supplier_material_multiple CHECK (order_multiple > 0),
    CONSTRAINT ck_supplier_material_lead_time CHECK (lead_time_days >= 0),
    CONSTRAINT ck_supplier_material_preferred CHECK (preferred IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商原材料供货关系表';

CREATE INDEX idx_supplier_material_supplier ON supplier_material (supplier_id);
CREATE INDEX idx_supplier_material_product ON supplier_material (product_id);
CREATE INDEX idx_supplier_material_preferred ON supplier_material (product_id, preferred);

INSERT INTO `bom_item` (finished_product_id, material_product_id, quantity_per_unit, loss_rate)
SELECT fp.id, rm.id, item.quantity_per_unit, item.loss_rate
FROM (
    SELECT 'FP001' AS finished_code, 'RM001' AS material_code, 0.0200 AS quantity_per_unit, 0.0300 AS loss_rate
    UNION ALL SELECT 'FP001', 'RM002', 0.0100, 0.0300
    UNION ALL SELECT 'FP001', 'RM004', 0.0040, 0.0200
    UNION ALL SELECT 'FP002', 'RM001', 0.0250, 0.0300
    UNION ALL SELECT 'FP002', 'RM003', 0.0400, 0.0500
    UNION ALL SELECT 'FP002', 'RM004', 0.0040, 0.0200
    UNION ALL SELECT 'FP003', 'RM001', 0.0300, 0.0300
    UNION ALL SELECT 'FP003', 'RM003', 0.0350, 0.0500
    UNION ALL SELECT 'FP003', 'RM004', 0.0040, 0.0200
    UNION ALL SELECT 'FP003', 'RM005', 0.0200, 0.0300
) item
JOIN product fp ON fp.code = item.finished_code
JOIN product rm ON rm.code = item.material_code;

INSERT INTO `supplier_material` (
    supplier_id,
    product_id,
    default_unit_price,
    min_order_quantity,
    order_multiple,
    lead_time_days,
    preferred,
    remark
)
SELECT s.id, p.id, p.cost_price, rule.min_order_quantity, rule.order_multiple, rule.lead_time_days, 1, '系统初始化供货关系'
FROM (
    SELECT '糯米供应商' AS supplier_name, 'RM001' AS product_code, 1 AS min_order_quantity, 1 AS order_multiple, 3 AS lead_time_days
    UNION ALL SELECT '豆沙供应商', 'RM002', 1, 1, 2
    UNION ALL SELECT '鲜肉供应商', 'RM003', 5, 1, 1
    UNION ALL SELECT '粽叶供应商', 'RM004', 1, 1, 4
    UNION ALL SELECT '蛋黄生产商', 'RM005', 1, 1, 2
) rule
JOIN supplier s ON s.name = rule.supplier_name
JOIN product p ON p.code = rule.product_code;
