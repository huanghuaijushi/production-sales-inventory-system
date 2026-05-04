CREATE TABLE `permission` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(120) NOT NULL,
    name VARCHAR(120) NOT NULL,
    module VARCHAR(64) NOT NULL,
    description VARCHAR(255) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_permission_code UNIQUE (code),
    CONSTRAINT ck_permission_enabled CHECK (enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `role` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_role_code UNIQUE (code),
    CONSTRAINT ck_role_enabled CHECK (enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admin_user_role (
    admin_user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (admin_user_id, role_id),
    CONSTRAINT fk_admin_user_role_admin_user FOREIGN KEY (admin_user_id) REFERENCES admin_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_user_role_role FOREIGN KEY (role_id) REFERENCES `role` (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_admin_user_role_role_id ON admin_user_role (role_id);

CREATE TABLE role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES `role` (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES `permission` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_role_permission_permission_id ON role_permission (permission_id);

INSERT INTO `role` (code, name, description, enabled, sort_order)
VALUES
    ('SUPER_ADMIN', 'Super Admin', 'Full system access', 1, 1),
    ('ADMIN', 'Admin', 'Daily operation access', 1, 10);

INSERT INTO `permission` (code, name, module, description, enabled, sort_order)
VALUES
    ('dashboard:view', 'View Dashboard', 'dashboard', 'View operation dashboard', 1, 10),
    ('system:user:view', 'View Admin Users', 'system', 'View administrator accounts', 1, 1010),
    ('system:user:create', 'Create Admin Users', 'system', 'Create administrator accounts', 1, 1020),
    ('system:user:update', 'Update Admin Users', 'system', 'Update administrator accounts', 1, 1030),
    ('system:user:disable', 'Disable Admin Users', 'system', 'Disable administrator accounts', 1, 1040),
    ('system:role:view', 'View Roles', 'system', 'View roles and permissions', 1, 1110),
    ('system:role:create', 'Create Roles', 'system', 'Create roles', 1, 1120),
    ('system:role:update', 'Update Roles', 'system', 'Update roles and permission bindings', 1, 1130),
    ('system:role:delete', 'Delete Roles', 'system', 'Delete roles', 1, 1140);

INSERT INTO role_permission (role_id, permission_id)
SELECT role_table.id, permission_table.id
FROM `role` role_table
CROSS JOIN `permission` permission_table
WHERE role_table.code = 'SUPER_ADMIN';

INSERT INTO role_permission (role_id, permission_id)
SELECT role_table.id, permission_table.id
FROM `role` role_table
CROSS JOIN `permission` permission_table
WHERE role_table.code = 'ADMIN'
  AND permission_table.code = 'dashboard:view';

INSERT INTO admin_user_role (admin_user_id, role_id)
SELECT admin_user.id, role_table.id
FROM admin_user
JOIN `role` role_table ON role_table.code = 'SUPER_ADMIN';
