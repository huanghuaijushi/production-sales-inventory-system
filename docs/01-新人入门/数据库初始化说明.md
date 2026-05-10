# Database Setup

本项目后端默认连接本机 MySQL：

```text
database: production_sales_inventory
username: psi_user
password: psi_password
hosts: localhost, 127.0.0.1
```

## Local MySQL

使用 root 账号初始化本地数据库：

```sql
CREATE DATABASE IF NOT EXISTS production_sales_inventory
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'psi_user'@'localhost'
  IDENTIFIED BY 'psi_password';
ALTER USER 'psi_user'@'localhost'
  IDENTIFIED BY 'psi_password';

CREATE USER IF NOT EXISTS 'psi_user'@'127.0.0.1'
  IDENTIFIED BY 'psi_password';
ALTER USER 'psi_user'@'127.0.0.1'
  IDENTIFIED BY 'psi_password';

GRANT ALL PRIVILEGES ON production_sales_inventory.*
  TO 'psi_user'@'localhost';
GRANT ALL PRIVILEGES ON production_sales_inventory.*
  TO 'psi_user'@'127.0.0.1';

FLUSH PRIVILEGES;
```

不建议给 `psi_user` 授权到 `%`，本地开发只需要 `localhost` 和 `127.0.0.1`。

## Migration

数据库表结构由 Flyway 管理，迁移脚本位于：

```text
production-sales-inventory-api/src/main/resources/db/migration
```

后端启动时会自动执行未运行的迁移。

当前认证相关迁移：

```text
V1__create_admin_user.sql
V2__create_role_permission_tables.sql
V3__create_inventory_tables.sql
V4__create_supplier_purchase_tables.sql
V5__create_bom_supplier_material_tables.sql
```

`V2` 会创建 RBAC 相关表：`role`、`permission`、`admin_user_role`、`role_permission`，并将历史 `admin_user` 数据迁移绑定到 `SUPER_ADMIN` 角色。

`V5` 会创建生产采购计划相关表：`bom_item`、`supplier_material`，用于成品配方、原材料供货规则、生产建议和智能采购建议。
