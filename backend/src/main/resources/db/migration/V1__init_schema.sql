CREATE TABLE sys_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(64) NOT NULL UNIQUE,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    email VARCHAR(120),
    password VARCHAR(128) NOT NULL,
    is_system_admin TINYINT(1) NOT NULL DEFAULT 0,
    status TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    role_code VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_department (user_id, department_id),
    CONSTRAINT fk_ud_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_ud_department FOREIGN KEY (department_id) REFERENCES sys_department (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE skill_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type VARCHAR(20) NOT NULL COMMENT 'SKILL/MCP',
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    scope_level VARCHAR(20) NOT NULL COMMENT 'PUBLIC/DEPARTMENT/PERSONAL',
    owner_department_id BIGINT NULL,
    owner_user_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/ACTIVE/DISABLED',
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    approval_required TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_resource_type (resource_type),
    INDEX idx_scope_level (scope_level),
    INDEX idx_owner_department (owner_department_id),
    INDEX idx_owner_user (owner_user_id),
    CONSTRAINT fk_resource_owner_department FOREIGN KEY (owner_department_id) REFERENCES sys_department (id),
    CONSTRAINT fk_resource_owner_user FOREIGN KEY (owner_user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_resource_created_by FOREIGN KEY (created_by) REFERENCES sys_user (id),
    CONSTRAINT fk_resource_updated_by FOREIGN KEY (updated_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE skill_config (
    resource_id BIGINT PRIMARY KEY,
    version VARCHAR(50),
    manifest_json JSON NOT NULL,
    entrypoint VARCHAR(255),
    icon VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_config_resource FOREIGN KEY (resource_id) REFERENCES skill_resource (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE mcp_config (
    resource_id BIGINT PRIMARY KEY,
    server_name VARCHAR(100) NOT NULL,
    transport_type VARCHAR(20) NOT NULL,
    command_line VARCHAR(500),
    args_json JSON,
    env_json JSON,
    endpoint_url VARCHAR(255),
    headers_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_mcp_config_resource FOREIGN KEY (resource_id) REFERENCES skill_resource (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resource_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    target_scope VARCHAR(20) NOT NULL COMMENT 'PUBLIC/DEPARTMENT/PERSONAL',
    department_id BIGINT NULL,
    user_id BIGINT NULL,
    permission_type VARCHAR(20) NOT NULL COMMENT 'USE/MANAGE',
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_permission_resource (resource_id),
    INDEX idx_permission_scope (target_scope),
    INDEX idx_permission_department (department_id),
    INDEX idx_permission_user (user_id),
    CONSTRAINT fk_permission_resource FOREIGN KEY (resource_id) REFERENCES skill_resource (id),
    CONSTRAINT fk_permission_department FOREIGN KEY (department_id) REFERENCES sys_department (id),
    CONSTRAINT fk_permission_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_permission_created_by FOREIGN KEY (created_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE resource_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    applicant_user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
    review_comment VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    INDEX idx_apply_resource (resource_id),
    INDEX idx_apply_user (applicant_user_id),
    INDEX idx_apply_department (department_id),
    CONSTRAINT fk_apply_resource FOREIGN KEY (resource_id) REFERENCES skill_resource (id),
    CONSTRAINT fk_apply_user FOREIGN KEY (applicant_user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_apply_department FOREIGN KEY (department_id) REFERENCES sys_department (id),
    CONSTRAINT fk_apply_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_user_id BIGINT NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id BIGINT NULL,
    detail_json JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_operator (operator_user_id),
    CONSTRAINT fk_audit_operator FOREIGN KEY (operator_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
