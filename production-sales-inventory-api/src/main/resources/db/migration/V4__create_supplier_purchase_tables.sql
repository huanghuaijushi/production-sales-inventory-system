CREATE TABLE `supplier` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL COMMENT '供应商名称',
    contact_name VARCHAR(80) NULL COMMENT '联系人',
    phone VARCHAR(30) NULL COMMENT '联系电话',
    address VARCHAR(255) NULL COMMENT '地址',
    remark VARCHAR(500) NULL COMMENT '备注',
    enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_supplier_name UNIQUE (name),
    CONSTRAINT ck_supplier_enabled CHECK (enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

CREATE INDEX idx_supplier_enabled ON supplier (enabled);

CREATE TABLE `purchase_order` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL COMMENT '采购单号',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    status VARCHAR(30) NOT NULL COMMENT '状态：DRAFT-草稿, PENDING_INBOUND-待入库, INBOUNDED-已入库, CANCELLED-已取消',
    expected_arrival_date DATE NULL COMMENT '预计到货日期',
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '采购总金额',
    operator_id BIGINT NOT NULL COMMENT '经办人ID',
    operator_name VARCHAR(80) NOT NULL COMMENT '经办人姓名',
    remark VARCHAR(500) NULL COMMENT '备注',
    inbound_at DATETIME(6) NULL COMMENT '入库时间',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_purchase_order_no UNIQUE (order_no),
    CONSTRAINT fk_purchase_order_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_order_operator FOREIGN KEY (operator_id) REFERENCES admin_user (id) ON DELETE RESTRICT,
    CONSTRAINT ck_purchase_order_status CHECK (status IN ('DRAFT', 'PENDING_INBOUND', 'INBOUNDED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购单表';

CREATE INDEX idx_purchase_order_supplier ON purchase_order (supplier_id);
CREATE INDEX idx_purchase_order_status ON purchase_order (status);
CREATE INDEX idx_purchase_order_created_at ON purchase_order (created_at);

CREATE TABLE `purchase_order_item` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '采购单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_code VARCHAR(64) NOT NULL COMMENT '商品编码快照',
    product_name VARCHAR(120) NOT NULL COMMENT '商品名称快照',
    product_specification VARCHAR(64) NULL COMMENT '规格快照',
    product_unit VARCHAR(20) NOT NULL COMMENT '单位快照',
    quantity INT NOT NULL COMMENT '采购数量',
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '采购单价',
    amount DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '采购金额',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_order_item_order FOREIGN KEY (order_id) REFERENCES purchase_order (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_order_item_product FOREIGN KEY (product_id) REFERENCES product (id) ON DELETE RESTRICT,
    CONSTRAINT ck_purchase_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购单明细表';

CREATE INDEX idx_purchase_order_item_order ON purchase_order_item (order_id);
CREATE INDEX idx_purchase_order_item_product ON purchase_order_item (product_id);
