
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `token_version` int NOT NULL DEFAULT '0',
  `last_login_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_username` (`username`),
  KEY `idx_sys_user_status` (`status`),
  KEY `idx_sys_user_created_at` (`created_at`),
  CONSTRAINT `ck_sys_user_role` CHECK ((`role` = _utf8mb4'ADMIN')),
  CONSTRAINT `ck_sys_user_status` CHECK ((`status` in (_utf8mb4'ACTIVE',_utf8mb4'DISABLED'))),
  CONSTRAINT `ck_sys_user_token_version` CHECK ((`token_version` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `sys_user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`sys_user_id`,`role_id`),
  KEY `idx_sys_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_user_role_sys_user` FOREIGN KEY (`sys_user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sys_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bom_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bom_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `finished_product_id` bigint NOT NULL COMMENT '成品ID',
  `material_product_id` bigint NOT NULL COMMENT '原材料ID',
  `quantity_per_unit` decimal(12,4) NOT NULL COMMENT '生产1个成品需要的原材料数量',
  `loss_rate` decimal(6,4) NOT NULL DEFAULT '0.0000' COMMENT '损耗率，如0.0300表示3%',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bom_item_product_material` (`finished_product_id`,`material_product_id`),
  KEY `idx_bom_item_finished_product` (`finished_product_id`),
  KEY `idx_bom_item_material_product` (`material_product_id`),
  CONSTRAINT `fk_bom_item_finished_product` FOREIGN KEY (`finished_product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_bom_item_material_product` FOREIGN KEY (`material_product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_bom_item_loss_rate` CHECK (((`loss_rate` >= 0) and (`loss_rate` <= 1))),
  CONSTRAINT `ck_bom_item_quantity` CHECK ((`quantity_per_unit` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成品配方明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `module` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`),
  CONSTRAINT `ck_permission_enabled` CHECK ((`enabled` in (0,1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品编码',
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品类型：FINISHED_PRODUCT-成品, RAW_MATERIAL-原料',
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类（如：豆沙粽、肉粽、糯米、粽叶等）',
  `specification` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规格（如：100g/个、5kg/袋）',
  `unit` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位（个、袋、斤、kg等）',
  `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价',
  `alert_quantity` int NOT NULL DEFAULT '0' COMMENT '库存预警数量',
  `shelf_life_days` int DEFAULT NULL COMMENT '保质期（天）',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`code`),
  KEY `idx_product_type` (`type`),
  KEY `idx_product_category` (`category`),
  CONSTRAINT `ck_product_enabled` CHECK ((`enabled` in (0,1))),
  CONSTRAINT `ck_product_type` CHECK ((`type` in (_utf8mb4'FINISHED_PRODUCT',_utf8mb4'RAW_MATERIAL')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `production_material_issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_material_issue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `production_order_id` bigint NOT NULL COMMENT '生产工单ID',
  `material_plan_id` bigint NOT NULL COMMENT '用料计划ID',
  `material_product_id` bigint NOT NULL COMMENT '原材料ID',
  `stock_batch_id` bigint NOT NULL COMMENT '库存批次ID',
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号快照',
  `issued_quantity` int NOT NULL COMMENT '领料数量',
  `stock_record_id` bigint DEFAULT NULL COMMENT '库存流水ID',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人名称',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_production_material_issue_plan` (`material_plan_id`),
  KEY `fk_production_material_issue_product` (`material_product_id`),
  KEY `fk_production_material_issue_record` (`stock_record_id`),
  KEY `fk_production_material_issue_operator` (`operator_id`),
  KEY `idx_production_material_issue_order` (`production_order_id`),
  KEY `idx_production_material_issue_batch` (`stock_batch_id`),
  CONSTRAINT `fk_production_material_issue_batch` FOREIGN KEY (`stock_batch_id`) REFERENCES `stock_batch` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_production_material_issue_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`)
 ON DELETE RESTRICT,
  CONSTRAINT `fk_production_material_issue_order` FOREIGN KEY (`production_order_id`) REFERENCES `production_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_production_material_issue_plan` FOREIGN KEY (`material_plan_id`) REFERENCES `production_material_plan` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_production_material_issue_product` FOREIGN KEY (`material_product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_production_material_issue_record` FOREIGN KEY (`stock_record_id`) REFERENCES `stock_record` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产领料记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `production_material_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_material_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `production_order_id` bigint NOT NULL COMMENT '生产工单ID',
  `material_product_id` bigint NOT NULL COMMENT '原材料ID',
  `required_quantity` int NOT NULL COMMENT '计划需求数量',
  `issued_quantity` int NOT NULL DEFAULT '0' COMMENT '已领料数量',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_production_material_plan_order` (`production_order_id`),
  KEY `idx_production_material_plan_product` (`material_product_id`),
  CONSTRAINT `fk_production_material_plan_order` FOREIGN KEY (`production_order_id`) REFERENCES `production_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_production_material_plan_product` FOREIGN KEY (`material_product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产计划用料表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `production_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '生产工单号',
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成品批次号',
  `product_id` bigint NOT NULL COMMENT '成品ID',
  `planned_quantity` int NOT NULL COMMENT '计划生产数量',
  `completed_quantity` int NOT NULL DEFAULT '0' COMMENT '已完成数量',
  `inbound_quantity` int NOT NULL DEFAULT '0' COMMENT '已入库数量',
  `loss_quantity` int NOT NULL DEFAULT '0' COMMENT '过程损耗数量',
  `current_step` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前工序',
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PLANNED' COMMENT '状态',
  `planned_date` date DEFAULT NULL COMMENT '计划生产日期',
  `started_at` datetime(6) DEFAULT NULL COMMENT '开始时间',
  `completed_at` datetime(6) DEFAULT NULL COMMENT '完成时间',
  `operator_id` bigint NOT NULL COMMENT '创建人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建人名称',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_production_order_no` (`order_no`),
  UNIQUE KEY `uk_production_order_batch_no` (`batch_no`),
  KEY `fk_production_order_operator` (`operator_id`),
  KEY `idx_production_order_product` (`product_id`),
  KEY `idx_production_order_status` (`status`),
  KEY `idx_production_order_created_at` (`created_at`),
  CONSTRAINT `fk_production_order_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_production_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_production_order_status` CHECK ((`status` in (_utf8mb4'PLANNED',_utf8mb4'IN_PROGRESS',_utf8mb4'WAIT_INBOUND',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED'))),
  CONSTRAINT `ck_production_order_step` CHECK (((`current_step` is null) or (`current_step` in (_utf8mb4'PREPARATION',_utf8mb4'WRAPPING',_utf8mb4'COOKING',_utf8mb4'PACKAGING',_utf8mb4'STERILIZATION',_utf8mb4'BOXING'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产工单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `production_step_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_step_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `production_order_id` bigint NOT NULL COMMENT '生产工单ID',
  `step_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工序',
  `completed_quantity` int NOT NULL DEFAULT '0' COMMENT '该次记录完成数量',
  `loss_quantity` int NOT NULL DEFAULT '0' COMMENT '该次记录损耗数量',
  `loss_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '损耗原因',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人名称',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `fk_production_step_record_operator` (`operator_id`),
  KEY `idx_production_step_record_order` (`production_order_id`),
  KEY `idx_production_step_record_created_at` (`created_at`),
  CONSTRAINT `fk_production_step_record_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_production_step_record_order` FOREIGN KEY (`production_order_id`) REFERENCES `production_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_production_step_type` CHECK ((`step_type` in (_utf8mb4'PREPARATION',_utf8mb4'WRAPPING',_utf8mb4'COOKING',_utf8mb4'PACKAGING',_utf8mb4'STERILIZATION',_utf8mb4'BOXING')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产工序记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态：DRAFT-草稿, PENDING_INBOUND-待入库, INBOUNDED-已入库, CANCELLED-已取消',
  `expected_arrival_date` date DEFAULT NULL COMMENT '预计到货日期',
  `total_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '采购总金额',
  `operator_id` bigint NOT NULL COMMENT '经办人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '经办人姓名',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `inbound_at` datetime(6) DEFAULT NULL COMMENT '入库时间',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_purchase_order_no` (`order_no`),
  KEY `fk_purchase_order_operator` (`operator_id`),
  KEY `idx_purchase_order_supplier` (`supplier_id`),
  KEY `idx_purchase_order_status` (`status`),
  KEY `idx_purchase_order_created_at` (`created_at`),
  CONSTRAINT `fk_purchase_order_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_purchase_order_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_purchase_order_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'PENDING_INBOUND',_utf8mb4'INBOUNDED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `purchase_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '采购单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码快照',
  `product_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称快照',
  `product_specification` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '规格快照',
  `product_unit` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位快照',
  `quantity` int NOT NULL COMMENT '采购数量',
  `unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '采购单价',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '采购金额',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_purchase_order_item_order` (`order_id`),
  KEY `idx_purchase_order_item_product` (`product_id`),
  CONSTRAINT `fk_purchase_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_purchase_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_purchase_order_item_quantity` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `sort_order` int NOT NULL DEFAULT '0',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`),
  CONSTRAINT `ck_role_enabled` CHECK ((`enabled` in (0,1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_permission` (
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `idx_role_permission_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `channel` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售渠道：DOUYIN-抖音, PINDUODUO-拼多多, OFFLINE-线下',
  `customer_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户名称',
  `customer_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户电话',
  `customer_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货地址',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单状态：PENDING-待发货, SHIPPED-已发货, COMPLETED-已完成, CANCELLED-已取消',
  `order_date` datetime(6) NOT NULL COMMENT '下单时间',
  `ship_date` datetime(6) DEFAULT NULL COMMENT '发货时间',
  `complete_date` datetime(6) DEFAULT NULL COMMENT '完成时间',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_order_no` (`order_no`),
  KEY `fk_sales_order_operator` (`operator_id`),
  KEY `idx_sales_order_channel` (`channel`),
  KEY `idx_sales_order_status` (`status`),
  KEY `idx_sales_order_date` (`order_date`),
  KEY `idx_sales_order_status_order_date` (`status`,`order_date`),
  CONSTRAINT `fk_sales_order_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_sales_order_channel` CHECK ((`channel` in (_utf8mb4'DOUYIN',_utf8mb4'PINDUODUO',_utf8mb4'OFFLINE',_utf8mb4'WECHAT_GROUP',_utf8mb4'CONTRACT'))),
  CONSTRAINT `ck_sales_order_status` CHECK ((`status` in (_utf8mb4'PENDING',_utf8mb4'SHIPPED',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sales_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `sales_goods_id` bigint NOT NULL COMMENT '销售商品ID',
  `goods_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售商品编码快照',
  `goods_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售商品名称快照',
  `goods_specification` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售商品规格快照',
  `goods_unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售商品单位快照',
  `goods_category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售商品分类快照',
  `external_product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部商品名称',
  `external_spec_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部规格名称',
  `external_quantity` decimal(12,4) DEFAULT NULL COMMENT '外部数量',
  `mapping_id` bigint DEFAULT NULL COMMENT '商品匹配规则ID',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_sales_order_item_order` (`order_id`),
  KEY `idx_sales_order_item_goods` (`sales_goods_id`),
  KEY `idx_sales_order_item_mapping` (`mapping_id`),
  CONSTRAINT `fk_sales_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `sales_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '当前库存数量',
  `locked_quantity` int NOT NULL DEFAULT '0' COMMENT '锁定数量（已下单未发货）',
  `available_quantity` int NOT NULL DEFAULT '0' COMMENT '可用数量',
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_product` (`product_id`),
  CONSTRAINT `fk_stock_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stock_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次号',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '到期日期',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '批次数量',
  `available_quantity` int NOT NULL DEFAULT '0' COMMENT '批次可用数量',
  `unit_cost` decimal(12,2) DEFAULT NULL COMMENT '批次单位成本快照',
  `source_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源类型：PURCHASE_ORDER、PRODUCTION_ORDER、INVENTORY_ADJUST 等',
  `source_order_id` bigint DEFAULT NULL COMMENT '来源单据ID',
  `source_order_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源单据号快照',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_stock_batch_product` (`product_id`),
  KEY `idx_stock_batch_batch_no` (`batch_no`),
  KEY `idx_stock_batch_expiry_date` (`expiry_date`),
  KEY `idx_stock_batch_source` (`source_type`,`source_order_id`),
  CONSTRAINT `fk_stock_batch_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_stock_batch_available_quantity` CHECK ((`available_quantity` >= 0)),
  CONSTRAINT `ck_stock_batch_quantity` CHECK ((`quantity` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='批次库存表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `stock_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单据号',
  `product_id` bigint NOT NULL COMMENT '产品ID',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型：IN-入库, OUT-出库, ADJUST-调整',
  `sub_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '子类型：PRODUCTION-生产入库, PURCHASE-采购入库, SALES-销售出库, LOSS-损耗, INVENTORY-盘点调整',
  `quantity` int NOT NULL COMMENT '数量（正数为入库，负数为出库）',
  `unit_price` decimal(12,2) DEFAULT NULL COMMENT '兼容字段：优先保存业务单价，无业务单价时保存成本单价',
  `amount` decimal(14,2) DEFAULT NULL COMMENT '兼容字段：优先保存业务金额，无业务金额时保存成本金额',
  `business_unit_price` decimal(12,2) DEFAULT NULL COMMENT '业务单价：销售出库/收费出库使用',
  `business_amount` decimal(14,2) DEFAULT NULL COMMENT '业务金额：业务单价 * 绝对数量',
  `cost_unit_price` decimal(12,2) DEFAULT NULL COMMENT '成本单价：采购、生产、领料、报损、盘点使用',
  `cost_amount` decimal(14,2) DEFAULT NULL COMMENT '成本金额：成本单价 * 绝对数量',
  `before_quantity` int NOT NULL COMMENT '操作前数量',
  `after_quantity` int NOT NULL COMMENT '操作后数量',
  `related_order_id` bigint DEFAULT NULL COMMENT '关联订单ID（如销售订单、生产计划等）',
  `related_order_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联单据类型：PURCHASE_ORDER、SALES_ORDER、PRODUCTION_ORDER 等',
  `related_order_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联单据号快照',
  `batch_id` bigint DEFAULT NULL COMMENT '批次ID',
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '批次号',
  `production_date` date DEFAULT NULL COMMENT '生产日期',
  `expiry_date` date DEFAULT NULL COMMENT '到期日期',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人姓名',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_record_no` (`record_no`),
  KEY `fk_stock_record_operator` (`operator_id`),
  KEY `idx_stock_record_product` (`product_id`),
  KEY `idx_stock_record_type` (`type`),
  KEY `idx_stock_record_created_at` (`created_at`),
  KEY `idx_stock_record_batch_no` (`batch_no`),
  KEY `idx_stock_record_batch_id` (`batch_id`),
  KEY `idx_stock_record_created_type_subtype` (`created_at`,`type`,`sub_type`),
  KEY `idx_stock_record_product_created` (`product_id`,`created_at`),
  KEY `idx_stock_record_related_order` (`related_order_type`,`related_order_id`),
  CONSTRAINT `fk_stock_record_batch` FOREIGN KEY (`batch_id`) REFERENCES `stock_batch` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_stock_record_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_stock_record_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_stock_record_sub_type` CHECK ((`sub_type` in (_utf8mb4'PRODUCTION',_utf8mb4'PURCHASE',_utf8mb4'SALES',_utf8mb4'PRODUCTION_USAGE',_utf8mb4'PRODUCTION_LOSS',_utf8mb4'PACKAGING_LOSS',_utf8mb4'SHIPPING_LOSS',_utf8mb4'INVENTORY'))),
  CONSTRAINT `ck_stock_record_type` CHECK ((`type` in (_utf8mb4'IN',_utf8mb4'OUT',_utf8mb4'ADJUST')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入库记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
  `contact_name` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_name` (`name`),
  KEY `idx_supplier_enabled` (`enabled`),
  CONSTRAINT `ck_supplier_enabled` CHECK ((`enabled` in (0,1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `supplier_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_material` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `product_id` bigint NOT NULL COMMENT '原材料ID',
  `default_unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '默认采购单价',
  `min_order_quantity` int NOT NULL DEFAULT '1' COMMENT '最小起订量',
  `order_multiple` int NOT NULL DEFAULT '1' COMMENT '采购倍数',
  `lead_time_days` int NOT NULL DEFAULT '0' COMMENT '预计交期天数',
  `preferred` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否优先供应商',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_material` (`supplier_id`,`product_id`),
  KEY `idx_supplier_material_supplier` (`supplier_id`),
  KEY `idx_supplier_material_product` (`product_id`),
  KEY `idx_supplier_material_preferred` (`product_id`,`preferred`),
  CONSTRAINT `fk_supplier_material_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_supplier_material_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_supplier_material_lead_time` CHECK ((`lead_time_days` >= 0)),
  CONSTRAINT `ck_supplier_material_min_order` CHECK ((`min_order_quantity` > 0)),
  CONSTRAINT `ck_supplier_material_multiple` CHECK ((`order_multiple` > 0)),
  CONSTRAINT `ck_supplier_material_preferred` CHECK ((`preferred` in (0,1))),
  CONSTRAINT `ck_supplier_material_price` CHECK ((`default_unit_price` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商原材料供货关系表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

CREATE TABLE IF NOT EXISTS `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '适用产品类型：FINISHED_PRODUCT-成品, RAW_MATERIAL-原料，NULL-通用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_category_name_type` (`name`, `type`),
  KEY `idx_product_category_type_enabled` (`type`, `enabled`),
  CONSTRAINT `ck_product_category_enabled` CHECK ((`enabled` in (0,1))),
  CONSTRAINT `ck_product_category_type` CHECK (((`type` is null) or (`type` in (_utf8mb4'FINISHED_PRODUCT',_utf8mb4'RAW_MATERIAL'))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品分类表';

INSERT INTO product_category (name, type, sort_order, enabled, remark)
VALUES
  ('粽子', 'FINISHED_PRODUCT', 10, 1, '成品粽子'),
  ('半成品', 'FINISHED_PRODUCT', 20, 1, '生产过程半成品'),
  ('原料', 'RAW_MATERIAL', 10, 1, '生产原材料'),
  ('包装', 'RAW_MATERIAL', 20, 1, '包装与辅材')
ON DUPLICATE KEY UPDATE sort_order = VALUES(sort_order), enabled = VALUES(enabled), remark = VALUES(remark);

CREATE TABLE IF NOT EXISTS `sales_channel_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码',
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
  `source_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '默认来源类型：EXCEL、TEXT、MANUAL、CONTRACT、API',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `config_json` json DEFAULT NULL COMMENT '渠道扩展配置',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_channel_config_code` (`code`),
  KEY `idx_sales_channel_config_enabled` (`enabled`, `sort_order`),
  CONSTRAINT `ck_sales_channel_config_enabled` CHECK ((`enabled` in (0,1))),
  CONSTRAINT `ck_sales_channel_config_source_type` CHECK ((`source_type` in (_utf8mb4'EXCEL',_utf8mb4'TEXT',_utf8mb4'MANUAL',_utf8mb4'CONTRACT',_utf8mb4'API')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售渠道配置表';

CREATE TABLE IF NOT EXISTS `sales_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售商品编码',
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售商品名称',
  `category` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售商品分类',
  `specification` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '销售商品规格',
  `unit` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售单位',
  `default_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '默认销售价',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_goods_code` (`code`),
  KEY `idx_sales_goods_enabled` (`enabled`, `code`),
  KEY `idx_sales_goods_category` (`category`),
  CONSTRAINT `ck_sales_goods_enabled` CHECK ((`enabled` in (0,1))),
  CONSTRAINT `ck_sales_goods_default_price` CHECK ((`default_price` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售商品表';

CREATE TABLE IF NOT EXISTS `sales_goods_component` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sales_goods_id` bigint NOT NULL COMMENT '销售商品ID',
  `product_id` bigint NOT NULL COMMENT '扣减的库存产品ID',
  `quantity_per_unit` decimal(12,4) NOT NULL COMMENT '销售1件商品消耗库存产品数量',
  `loss_rate` decimal(6,4) NOT NULL DEFAULT '0.0000' COMMENT '销售扣减损耗率',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_goods_component_product` (`sales_goods_id`,`product_id`),
  KEY `idx_sales_goods_component_product` (`product_id`),
  CONSTRAINT `fk_sales_goods_component_goods` FOREIGN KEY (`sales_goods_id`) REFERENCES `sales_goods` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sales_goods_component_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_sales_goods_component_quantity` CHECK ((`quantity_per_unit` > 0)),
  CONSTRAINT `ck_sales_goods_component_loss_rate` CHECK (((`loss_rate` >= 0) and (`loss_rate` <= 1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售商品库存组成表';

CREATE TABLE IF NOT EXISTS `sales_goods_channel_price` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sales_goods_id` bigint NOT NULL COMMENT '销售商品ID',
  `channel_id` bigint NOT NULL COMMENT '销售渠道ID',
  `price` decimal(10,2) NOT NULL COMMENT '渠道销售价',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sales_goods_channel_price` (`sales_goods_id`,`channel_id`),
  KEY `idx_sales_goods_channel_price_channel` (`channel_id`, `enabled`),
  CONSTRAINT `fk_sales_goods_channel_price_goods` FOREIGN KEY (`sales_goods_id`) REFERENCES `sales_goods` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sales_goods_channel_price_channel` FOREIGN KEY (`channel_id`) REFERENCES `sales_channel_config` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_sales_goods_channel_price_enabled` CHECK ((`enabled` in (0,1))),
  CONSTRAINT `ck_sales_goods_channel_price_price` CHECK ((`price` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售商品渠道价格表';

ALTER TABLE `sales_order_item`
  ADD CONSTRAINT `fk_sales_order_item_goods` FOREIGN KEY (`sales_goods_id`) REFERENCES `sales_goods` (`id`) ON DELETE RESTRICT;

CREATE TABLE IF NOT EXISTS `order_import_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导入批次号',
  `channel_id` bigint NOT NULL COMMENT '销售渠道ID',
  `source_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '来源类型',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导入文件名',
  `raw_text` mediumtext COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '导入原始文本',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '订单总数',
  `parsed_count` int NOT NULL DEFAULT '0' COMMENT '解析成功数',
  `ready_count` int NOT NULL DEFAULT '0' COMMENT '可确认数',
  `converted_count` int NOT NULL DEFAULT '0' COMMENT '已转单数',
  `error_count` int NOT NULL DEFAULT '0' COMMENT '异常数',
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作人名称',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_import_batch_no` (`batch_no`),
  KEY `idx_order_import_batch_channel` (`channel_id`),
  KEY `idx_order_import_batch_status` (`status`),
  KEY `idx_order_import_batch_created_at` (`created_at`),
  CONSTRAINT `fk_order_import_batch_channel` FOREIGN KEY (`channel_id`) REFERENCES `sales_channel_config` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_order_import_batch_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_order_import_batch_source_type` CHECK ((`source_type` in (_utf8mb4'EXCEL',_utf8mb4'TEXT',_utf8mb4'MANUAL',_utf8mb4'CONTRACT',_utf8mb4'API'))),
  CONSTRAINT `ck_order_import_batch_status` CHECK ((`status` in (_utf8mb4'DRAFT',_utf8mb4'PARSED',_utf8mb4'CONFIRMED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单导入批次表';

CREATE TABLE IF NOT EXISTS `external_order_raw` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL COMMENT '导入批次ID',
  `channel_id` bigint NOT NULL COMMENT '销售渠道ID',
  `external_order_no` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '外部订单号',
  `raw_payload` json DEFAULT NULL COMMENT '外部订单原始JSON',
  `order_time` datetime(6) DEFAULT NULL COMMENT '外部下单时间',
  `customer_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户名称',
  `customer_phone` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户电话',
  `customer_address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收货地址',
  `province` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省',
  `city` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '市',
  `district` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区县',
  `logistics_company` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流公司',
  `tracking_no` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物流单号',
  `buyer_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '买家留言',
  `seller_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '卖家备注',
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WAIT_MATCH' COMMENT '状态',
  `error_message` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `sales_order_id` bigint DEFAULT NULL COMMENT '生成的销售单ID',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_external_order_channel_no` (`channel_id`, `external_order_no`),
  KEY `idx_external_order_batch` (`batch_id`),
  KEY `idx_external_order_status` (`status`),
  KEY `idx_external_order_sales_order` (`sales_order_id`),
  CONSTRAINT `fk_external_order_batch` FOREIGN KEY (`batch_id`) REFERENCES `order_import_batch` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_external_order_channel` FOREIGN KEY (`channel_id`) REFERENCES `sales_channel_config` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_external_order_sales_order` FOREIGN KEY (`sales_order_id`) REFERENCES `sales_order` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_external_order_status` CHECK ((`status` in (_utf8mb4'WAIT_MATCH',_utf8mb4'READY',_utf8mb4'ERROR',_utf8mb4'CONVERTED',_utf8mb4'SKIPPED')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部原始订单表';

CREATE TABLE IF NOT EXISTS `sales_goods_match_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `channel_id` bigint NOT NULL COMMENT '销售渠道ID',
  `external_product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '外部商品名称',
  `external_spec_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部规格名称',
  `external_sku_code` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部SKU编码',
  `sales_goods_id` bigint NOT NULL COMMENT '匹配到的销售商品ID',
  `quantity_multiplier` decimal(12,4) NOT NULL DEFAULT '1.0000' COMMENT '数量换算倍数',
  `default_unit_price` decimal(10,2) DEFAULT NULL COMMENT '外部销售规格默认成交价',
  `match_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'EXACT' COMMENT '匹配类型',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `priority` int NOT NULL DEFAULT '100' COMMENT '优先级，数值越小越优先',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_sales_goods_match_rule_channel` (`channel_id`, `enabled`, `priority`),
  KEY `idx_sales_goods_match_rule_goods` (`sales_goods_id`),
  CONSTRAINT `fk_sales_goods_match_rule_channel` FOREIGN KEY (`channel_id`) REFERENCES `sales_channel_config` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_sales_goods_match_rule_goods` FOREIGN KEY (`sales_goods_id`) REFERENCES `sales_goods` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `ck_sales_goods_match_rule_enabled` CHECK ((`enabled` in (0,1))),
  CONSTRAINT `ck_sales_goods_match_rule_multiplier` CHECK ((`quantity_multiplier` > 0)),
  CONSTRAINT `ck_sales_goods_match_rule_match_type` CHECK ((`match_type` in (_utf8mb4'EXACT',_utf8mb4'CONTAINS')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部商品匹配销售商品规则表';

CREATE TABLE IF NOT EXISTS `external_order_item_raw` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `external_order_id` bigint NOT NULL COMMENT '外部原始订单ID',
  `external_product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '外部商品名称',
  `external_spec_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部规格名称',
  `external_sku_code` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部SKU编码',
  `external_quantity` decimal(12,4) NOT NULL DEFAULT '1.0000' COMMENT '外部数量',
  `external_unit_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '外部单价',
  `matched_sales_goods_id` bigint DEFAULT NULL COMMENT '匹配到的销售商品ID',
  `matched_goods_code` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '匹配销售商品编码快照',
  `matched_goods_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '匹配销售商品名称快照',
  `mapping_id` bigint DEFAULT NULL COMMENT '使用的映射规则ID',
  `converted_quantity` int DEFAULT NULL COMMENT '换算后的系统数量',
  `match_status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'UNMATCHED' COMMENT '匹配状态',
  `match_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '匹配说明',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  KEY `idx_external_order_item_order` (`external_order_id`),
  KEY `idx_external_order_item_matched_goods` (`matched_sales_goods_id`),
  KEY `idx_external_order_item_mapping` (`mapping_id`),
  KEY `idx_external_order_item_match_status` (`match_status`),
  CONSTRAINT `fk_external_order_item_order` FOREIGN KEY (`external_order_id`) REFERENCES `external_order_raw` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_external_order_item_sales_goods` FOREIGN KEY (`matched_sales_goods_id`) REFERENCES `sales_goods` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_external_order_item_mapping` FOREIGN KEY (`mapping_id`) REFERENCES `sales_goods_match_rule` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_external_order_item_match_status` CHECK ((`match_status` in (_utf8mb4'MATCHED',_utf8mb4'UNMATCHED',_utf8mb4'AMBIGUOUS',_utf8mb4'ERROR'))),
  CONSTRAINT `ck_external_order_item_quantity` CHECK ((`external_quantity` > 0)),
  CONSTRAINT `ck_external_order_item_converted_quantity` CHECK (((`converted_quantity` is null) or (`converted_quantity` > 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部原始订单明细表';

ALTER TABLE `sales_order`
  ADD COLUMN `channel_id` bigint DEFAULT NULL COMMENT '销售渠道配置ID' AFTER `channel`,
  ADD COLUMN `external_order_no` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '外部订单号' AFTER `channel_id`,
  ADD COLUMN `import_batch_id` bigint DEFAULT NULL COMMENT '导入批次ID' AFTER `external_order_no`,
  ADD COLUMN `source_type` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单来源类型' AFTER `import_batch_id`,
  ADD COLUMN `source_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源备注' AFTER `source_type`,
  ADD KEY `idx_sales_order_channel_id` (`channel_id`),
  ADD KEY `idx_sales_order_import_batch` (`import_batch_id`),
  ADD KEY `idx_sales_order_external_order` (`channel_id`, `external_order_no`),
  ADD CONSTRAINT `fk_sales_order_channel_config` FOREIGN KEY (`channel_id`) REFERENCES `sales_channel_config` (`id`) ON DELETE RESTRICT,
  ADD CONSTRAINT `fk_sales_order_import_batch` FOREIGN KEY (`import_batch_id`) REFERENCES `order_import_batch` (`id`) ON DELETE SET NULL;

ALTER TABLE `sales_order_item`
  ADD CONSTRAINT `fk_sales_order_item_mapping` FOREIGN KEY (`mapping_id`) REFERENCES `sales_goods_match_rule` (`id`) ON DELETE SET NULL;

INSERT INTO sales_channel_config (code, name, source_type, enabled, sort_order, remark)
VALUES
  ('OFFLINE', '线下', 'MANUAL', 1, 10, '系统内手工销售单'),
  ('PINDUODUO', '拼多多', 'EXCEL', 1, 20, '拼多多订单导入'),
  ('DOUYIN', '抖音', 'EXCEL', 1, 30, '抖店订单导入预留'),
  ('WECHAT_GROUP', '微信群', 'TEXT', 1, 40, '微信群文本订单导入'),
  ('CUSTOMER_CHAT', '客服聊天', 'TEXT', 1, 41, '客服聊天/短信文本订单导入'),
  ('TEXT_RETAIL', '零售文本订单', 'TEXT', 1, 42, '通用粘贴文本订单导入'),
  ('CONTRACT', '合同客户', 'CONTRACT', 1, 50, '合同订单预留')
ON DUPLICATE KEY UPDATE name = VALUES(name), source_type = VALUES(source_type), enabled = VALUES(enabled), sort_order = VALUES(sort_order), remark = VALUES(remark);

UPDATE sales_order so
JOIN sales_channel_config sc ON sc.code = so.channel
SET so.channel_id = sc.id,
    so.source_type = CASE so.channel WHEN 'OFFLINE' THEN 'MANUAL' ELSE 'EXCEL' END
WHERE so.channel_id IS NULL;

INSERT INTO `role` (code, name, description, enabled, sort_order)
VALUES
  ('SUPER_ADMIN', '超级管理员', '系统默认超级管理员角色', 1, 0),
  ('ADMIN', '管理员', '系统管理与业务管理角色', 1, 10),
  ('WAREHOUSE', '仓库', '库存、入库、出库、批次管理角色', 1, 20),
  ('FINANCE', '财务', '金额、成本、利润查看角色', 1, 30),
  ('MANAGER', '主管', '全局业务查看与审核角色', 1, 40),
  ('MERCHANDISER', '跟单', '销售跟单与订单协同角色', 1, 50),
  ('PURCHASER', '采购', '采购与供应商管理角色', 1, 60),
  ('SALES', '销售', '销售订单与客户管理角色', 1, 70),
  ('PRODUCTION_MANAGER', '生产主管', '生产计划、领料、报工管理角色', 1, 80)
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description), enabled = VALUES(enabled), sort_order = VALUES(sort_order);

INSERT INTO `permission` (code, name, module, description, enabled, sort_order)
VALUES
  ('*', '全部权限', 'SYSTEM', '系统全部操作权限', 1, 0),
  ('auth:user:view', '查看用户', 'SYSTEM', '查看系统用户信息', 1, 10),
  ('auth:user:create', '新增用户', 'SYSTEM', '创建系统用户', 1, 20),
  ('auth:user:update', '编辑用户', 'SYSTEM', '修改系统用户资料', 1, 30),
  ('auth:user:disable', '禁用用户', 'SYSTEM', '禁用系统用户', 1, 40),
  ('auth:role:view', '查看角色', 'SYSTEM', '查看角色信息', 1, 50),
  ('auth:role:update', '编辑角色', 'SYSTEM', '维护角色与权限关系', 1, 60),
  ('auth:permission:view', '查看权限', 'SYSTEM', '查看权限点', 1, 70),
  ('product:view', '查看商品', 'PRODUCT', '查看商品资料', 1, 100),
  ('product:create', '新增商品', 'PRODUCT', '新增商品资料', 1, 110),
  ('product:update', '编辑商品', 'PRODUCT', '修改商品资料', 1, 120),
  ('product:delete', '删除商品', 'PRODUCT', '删除商品资料', 1, 130),
  ('stock:view', '查看库存', 'INVENTORY', '查看库存汇总', 1, 200),
  ('stock:in', '库存入库', 'INVENTORY', '登记入库', 1, 210),
  ('stock:out', '库存出库', 'INVENTORY', '登记出库', 1, 220),
  ('stock:adjust', '库存调整', 'INVENTORY', '调整库存', 1, 230),
  ('stock:record:view', '查看库存流水', 'INVENTORY', '查看出入库记录', 1, 240),
  ('stock:batch:view', '查看批次库存', 'INVENTORY', '查看批次库存', 1, 250),
  ('purchase:view', '查看采购', 'PURCHASE', '查看采购单', 1, 300),
  ('purchase:create', '新增采购', 'PURCHASE', '创建采购单', 1, 310),
  ('purchase:update', '编辑采购', 'PURCHASE', '修改采购单', 1, 320),
  ('purchase:inbound', '采购入库', 'PURCHASE', '采购单入库', 1, 340),
  ('purchase:cancel', '取消采购', 'PURCHASE', '取消采购单', 1, 350),
  ('sales:view', '查看销售', 'SALES', '查看销售订单', 1, 400),
  ('sales:create', '新增销售', 'SALES', '创建销售订单', 1, 410),
  ('sales:update', '编辑销售', 'SALES', '修改销售订单', 1, 420),
  ('sales:ship', '发货', 'SALES', '销售发货', 1, 430),
  ('sales:complete', '完成销售', 'SALES', '销售订单完成确认', 1, 440),
  ('sales:cancel', '取消销售', 'SALES', '取消销售订单', 1, 450),
  ('sales:import', '导入销售', 'SALES', '导入外部订单', 1, 460),
  ('production:view', '查看生产', 'PRODUCTION', '查看生产工单', 1, 500),
  ('production:create', '新增生产', 'PRODUCTION', '创建生产工单', 1, 510),
  ('production:start', '开始生产', 'PRODUCTION', '开始生产工单', 1, 520),
  ('production:material-issue', '生产领料', 'PRODUCTION', '生产领料操作', 1, 530),
  ('production:step-report', '工序报工', 'PRODUCTION', '提交工序报工', 1, 540),
  ('production:inbound', '生产入库', 'PRODUCTION', '生产入库操作', 1, 550),
  ('production:cancel', '取消生产', 'PRODUCTION', '取消生产工单', 1, 570),
  ('finance:view-cost', '查看成本', 'FINANCE', '查看成本数据', 1, 600),
  ('finance:view-profit', '查看利润', 'FINANCE', '查看利润', 1, 620),
  ('dashboard:view', '查看看板', 'DASHBOARD', '查看经营看板', 1, 700)
ON DUPLICATE KEY UPDATE name = VALUES(name), module = VALUES(module), description = VALUES(description), enabled = VALUES(enabled), sort_order = VALUES(sort_order);

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code = '*'
WHERE r.code = 'SUPER_ADMIN';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN (
  'auth:user:view','auth:user:create','auth:user:update','auth:user:disable',
  'auth:role:view','auth:role:update','auth:permission:view',
  'product:view','product:create','product:update','product:delete',
  'stock:view','stock:in','stock:out','stock:adjust','stock:record:view','stock:batch:view',
  'purchase:view','purchase:create','purchase:update','purchase:inbound','purchase:cancel',
  'sales:view','sales:create','sales:update','sales:ship','sales:complete','sales:cancel','sales:import',
  'production:view','production:create','production:start','production:material-issue','production:step-report','production:inbound','production:cancel',
  'finance:view-cost','finance:view-profit','dashboard:view'
)
WHERE r.code = 'ADMIN';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('stock:view','stock:in','stock:out','stock:adjust','stock:record:view','stock:batch:view','product:view')
WHERE r.code = 'WAREHOUSE';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('finance:view-cost','finance:view-profit','stock:view','sales:view','purchase:view','production:view','dashboard:view')
WHERE r.code = 'FINANCE';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('dashboard:view','product:view','stock:view','purchase:view','sales:view','production:view','finance:view-cost','finance:view-profit')
WHERE r.code = 'MANAGER';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('sales:view','sales:create','sales:update','sales:import','sales:ship','sales:complete','sales:cancel','dashboard:view')
WHERE r.code = 'MERCHANDISER';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('purchase:view','purchase:create','purchase:update','purchase:inbound','purchase:cancel','stock:view','stock:batch:view','dashboard:view')
WHERE r.code = 'PURCHASER';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('sales:view','sales:create','sales:update','sales:ship','sales:complete','sales:cancel','sales:import','purchase:view','dashboard:view')
WHERE r.code = 'SALES';

INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM `role` r
JOIN `permission` p ON p.code IN ('production:view','production:create','production:start','production:material-issue','production:step-report','production:inbound','production:cancel','stock:view','stock:record:view','dashboard:view')
WHERE r.code = 'PRODUCTION_MANAGER';

INSERT INTO `sys_user` (username, password_hash, nickname, role, status, token_version, last_login_at)
VALUES ('admin', '$2y$10$E7UdBJXyP4cqkPOsCBvxm.YuFhlyUlJXBOMHIZsIUgy3iAiBmOZFa', '系统管理员', 'ADMIN', 'ACTIVE', 0, NULL)
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  nickname = VALUES(nickname),
  role = VALUES(role),
  status = VALUES(status),
  token_version = VALUES(token_version);

INSERT IGNORE INTO `sys_user_role` (sys_user_id, role_id)
SELECT su.id, r.id
FROM `sys_user` su
JOIN `role` r ON r.code = 'SUPER_ADMIN'
WHERE su.username = 'admin';
