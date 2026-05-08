DELIMITER $$

DROP PROCEDURE IF EXISTS migrate_admin_user_to_sys_user$$

CREATE PROCEDURE migrate_admin_user_to_sys_user()
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'admin_user'
  ) AND NOT EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
  ) THEN
    RENAME TABLE `admin_user` TO `sys_user`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND constraint_name = 'ck_admin_user_role'
  ) THEN
    ALTER TABLE `sys_user` DROP CHECK `ck_admin_user_role`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND constraint_name = 'ck_admin_user_status'
  ) THEN
    ALTER TABLE `sys_user` DROP CHECK `ck_admin_user_status`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND constraint_name = 'ck_admin_user_token_version'
  ) THEN
    ALTER TABLE `sys_user` DROP CHECK `ck_admin_user_token_version`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'uk_admin_user_username'
  ) THEN
    ALTER TABLE `sys_user` DROP INDEX `uk_admin_user_username`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_admin_user_status'
  ) THEN
    ALTER TABLE `sys_user` DROP INDEX `idx_admin_user_status`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_admin_user_created_at'
  ) THEN
    ALTER TABLE `sys_user` DROP INDEX `idx_admin_user_created_at`;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND column_name = 'role'
  ) THEN
    ALTER TABLE `sys_user` DROP COLUMN `role`;
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'uk_sys_user_username'
  ) THEN
    ALTER TABLE `sys_user` ADD UNIQUE KEY `uk_sys_user_username` (`username`);
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_sys_user_status'
  ) THEN
    ALTER TABLE `sys_user` ADD KEY `idx_sys_user_status` (`status`);
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND index_name = 'idx_sys_user_created_at'
  ) THEN
    ALTER TABLE `sys_user` ADD KEY `idx_sys_user_created_at` (`created_at`);
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND constraint_name = 'ck_sys_user_status'
  ) THEN
    ALTER TABLE `sys_user` ADD CONSTRAINT `ck_sys_user_status` CHECK (`status` IN ('ACTIVE', 'DISABLED'));
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND constraint_name = 'ck_sys_user_token_version'
  ) THEN
    ALTER TABLE `sys_user` ADD CONSTRAINT `ck_sys_user_token_version` CHECK (`token_version` >= 0);
  END IF;

  CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `sys_user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`sys_user_id`, `role_id`),
    KEY `idx_sys_user_role_role_id` (`role_id`),
    CONSTRAINT `fk_sys_user_role_sys_user` FOREIGN KEY (`sys_user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sys_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

  INSERT IGNORE INTO `sys_user_role` (`sys_user_id`, `role_id`)
  SELECT su.id, r.id
  FROM `sys_user` su
  JOIN `role` r ON r.code = 'SUPER_ADMIN'
  WHERE su.username = 'admin';

  INSERT IGNORE INTO `sys_user_role` (`sys_user_id`, `role_id`)
  SELECT su.id, r.id
  FROM `sys_user` su
  JOIN `role` r ON r.code = 'ADMIN'
  WHERE su.username <> 'admin';

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'production_order' AND constraint_name = 'fk_production_order_operator'
  ) THEN
    ALTER TABLE `production_order` DROP FOREIGN KEY `fk_production_order_operator`;
  END IF;
  ALTER TABLE `production_order`
    ADD CONSTRAINT `fk_production_order_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'production_step_record' AND constraint_name = 'fk_production_step_record_operator'
  ) THEN
    ALTER TABLE `production_step_record` DROP FOREIGN KEY `fk_production_step_record_operator`;
  END IF;
  ALTER TABLE `production_step_record`
    ADD CONSTRAINT `fk_production_step_record_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'purchase_order' AND constraint_name = 'fk_purchase_order_operator'
  ) THEN
    ALTER TABLE `purchase_order` DROP FOREIGN KEY `fk_purchase_order_operator`;
  END IF;
  ALTER TABLE `purchase_order`
    ADD CONSTRAINT `fk_purchase_order_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'sales_order' AND constraint_name = 'fk_sales_order_operator'
  ) THEN
    ALTER TABLE `sales_order` DROP FOREIGN KEY `fk_sales_order_operator`;
  END IF;
  ALTER TABLE `sales_order`
    ADD CONSTRAINT `fk_sales_order_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'stock_record' AND constraint_name = 'fk_stock_record_operator'
  ) THEN
    ALTER TABLE `stock_record` DROP FOREIGN KEY `fk_stock_record_operator`;
  END IF;
  ALTER TABLE `stock_record`
    ADD CONSTRAINT `fk_stock_record_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;

  IF EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'order_import_batch' AND constraint_name = 'fk_order_import_batch_operator'
  ) THEN
    ALTER TABLE `order_import_batch` DROP FOREIGN KEY `fk_order_import_batch_operator`;
  END IF;
  ALTER TABLE `order_import_batch`
    ADD CONSTRAINT `fk_order_import_batch_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT;
END$$

CALL migrate_admin_user_to_sys_user()$$

DROP PROCEDURE IF EXISTS migrate_admin_user_to_sys_user$$

DELIMITER ;
