-- 产品表（包含成品和原料）
CREATE TABLE `product` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL COMMENT '产品编码',
    name VARCHAR(120) NOT NULL COMMENT '产品名称',
    type VARCHAR(20) NOT NULL COMMENT '产品类型：FINISHED_PRODUCT-成品, RAW_MATERIAL-原料',
    category VARCHAR(64) NULL COMMENT '分类（如：豆沙粽、肉粽、糯米、粽叶等）',
    specification VARCHAR(64) NULL COMMENT '规格（如：100g/个、5kg/袋）',
    unit VARCHAR(20) NOT NULL COMMENT '单位（个、袋、斤、kg等）',
    cost_price DECIMAL(10, 2) NULL COMMENT '成本价',
    sale_price DECIMAL(10, 2) NULL COMMENT '销售价',
    alert_quantity INT NOT NULL DEFAULT 0 COMMENT '库存预警数量',
    shelf_life_days INT NULL COMMENT '保质期（天）',
    description VARCHAR(500) NULL COMMENT '描述',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_product_code UNIQUE (code),
    CONSTRAINT ck_product_type CHECK (type IN ('FINISHED_PRODUCT', 'RAW_MATERIAL')),
    CONSTRAINT ck_product_enabled CHECK (enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

CREATE INDEX idx_product_type ON product (type);
CREATE INDEX idx_product_category ON product (category);

-- 库存表
CREATE TABLE `stock` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    quantity INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    locked_quantity INT NOT NULL DEFAULT 0 COMMENT '锁定数量（已下单未发货）',
    available_quantity INT NOT NULL DEFAULT 0 COMMENT '可用数量',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_stock_product UNIQUE (product_id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 出入库记录表
CREATE TABLE `stock_record` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    record_no VARCHAR(64) NOT NULL COMMENT '单据号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：IN-入库, OUT-出库, ADJUST-调整',
    sub_type VARCHAR(30) NULL COMMENT '子类型：PRODUCTION-生产入库, PURCHASE-采购入库, SALES-销售出库, LOSS-损耗, INVENTORY-盘点调整',
    quantity INT NOT NULL COMMENT '数量（正数为入库，负数为出库）',
    before_quantity INT NOT NULL COMMENT '操作前数量',
    after_quantity INT NOT NULL COMMENT '操作后数量',
    related_order_id BIGINT NULL COMMENT '关联订单ID（如销售订单、生产计划等）',
    batch_no VARCHAR(64) NULL COMMENT '批次号',
    production_date DATE NULL COMMENT '生产日期',
    expiry_date DATE NULL COMMENT '到期日期',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(80) NOT NULL COMMENT '操作人姓名',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_stock_record_no UNIQUE (record_no),
    CONSTRAINT fk_stock_record_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_record_operator FOREIGN KEY (operator_id) REFERENCES admin_user (id) ON DELETE RESTRICT,
    CONSTRAINT ck_stock_record_type CHECK (type IN ('IN', 'OUT', 'ADJUST')),
    CONSTRAINT ck_stock_record_sub_type CHECK (sub_type IN ('PRODUCTION', 'PURCHASE', 'SALES', 'LOSS', 'INVENTORY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入库记录表';

CREATE INDEX idx_stock_record_product ON stock_record (product_id);
CREATE INDEX idx_stock_record_type ON stock_record (type);
CREATE INDEX idx_stock_record_created_at ON stock_record (created_at);
CREATE INDEX idx_stock_record_batch_no ON stock_record (batch_no);

-- 销售订单表
CREATE TABLE `sales_order` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    channel VARCHAR(30) NOT NULL COMMENT '销售渠道：DOUYIN-抖音, PINDUODUO-拼多多, OFFLINE-线下',
    customer_name VARCHAR(120) NULL COMMENT '客户名称',
    customer_phone VARCHAR(20) NULL COMMENT '客户电话',
    customer_address VARCHAR(255) NULL COMMENT '收货地址',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) NOT NULL COMMENT '订单状态：PENDING-待发货, SHIPPED-已发货, COMPLETED-已完成, CANCELLED-已取消',
    order_date DATETIME(6) NOT NULL COMMENT '下单时间',
    ship_date DATETIME(6) NULL COMMENT '发货时间',
    complete_date DATETIME(6) NULL COMMENT '完成时间',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(80) NOT NULL COMMENT '操作人姓名',
    remark VARCHAR(500) NULL COMMENT '备注',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_sales_order_no UNIQUE (order_no),
    CONSTRAINT fk_sales_order_operator FOREIGN KEY (operator_id) REFERENCES admin_user (id) ON DELETE RESTRICT,
    CONSTRAINT ck_sales_order_channel CHECK (channel IN ('DOUYIN', 'PINDUODUO', 'OFFLINE')),
    CONSTRAINT ck_sales_order_status CHECK (status IN ('PENDING', 'SHIPPED', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单表';

CREATE INDEX idx_sales_order_channel ON sales_order (channel);
CREATE INDEX idx_sales_order_status ON sales_order (status);
CREATE INDEX idx_sales_order_date ON sales_order (order_date);

-- 销售订单明细表
CREATE TABLE `sales_order_item` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(120) NOT NULL COMMENT '产品名称',
    product_specification VARCHAR(64) NULL COMMENT '产品规格',
    quantity INT NOT NULL COMMENT '数量',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT '小计',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_sales_order_item_order FOREIGN KEY (order_id) REFERENCES sales_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_order_item_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单明细表';

CREATE INDEX idx_sales_order_item_order ON sales_order_item (order_id);
CREATE INDEX idx_sales_order_item_product ON sales_order_item (product_id);

-- 插入初始产品数据（示例）
INSERT INTO `product` (code, name, type, category, specification, unit, cost_price, sale_price, alert_quantity, shelf_life_days, enabled)
VALUES
    ('FP001', '豆沙粽', 'FINISHED_PRODUCT', '粽子', '100g/个', '个', 2.50, 5.00, 500, 7, 1),
    ('FP002', '鲜肉粽', 'FINISHED_PRODUCT', '粽子', '120g/个', '个', 3.00, 6.00, 500, 7, 1),
    ('FP003', '蛋黄肉粽', 'FINISHED_PRODUCT', '粽子', '150g/个', '个', 4.00, 8.00, 300, 7, 1),
    ('RM001', '糯米', 'RAW_MATERIAL', '原料', '25kg/袋', '袋', 120.00, NULL, 10, NULL, 1),
    ('RM002', '豆沙馅', 'RAW_MATERIAL', '原料', '5kg/袋', '袋', 35.00, NULL, 5, 30, 1),
    ('RM003', '猪肉', 'RAW_MATERIAL', '原料', '1kg', 'kg', 25.00, NULL, 20, 3, 1),
    ('RM004', '粽叶', 'RAW_MATERIAL', '原料', '500片/包', '包', 15.00, NULL, 10, NULL, 1),
    ('RM005', '咸蛋黄', 'RAW_MATERIAL', '原料', '50个/盒', '盒', 40.00, NULL, 5, 15, 1);

-- 初始化库存（数量为0）
INSERT INTO `stock` (product_id, quantity, locked_quantity, available_quantity)
SELECT id, 0, 0, 0 FROM `product`;
