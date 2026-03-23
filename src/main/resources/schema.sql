-- 1. 租户套餐表
CREATE TABLE IF NOT EXISTS sys_tenant_package (
    id BIGINT NOT NULL PRIMARY KEY,
    package_name VARCHAR(50) NOT NULL,
    menu_ids CLOB NOT NULL, -- H2 中使用 CLOB 存储长文本
    status SMALLINT DEFAULT 0,
    remark VARCHAR(500),
    create_by VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 租户表
CREATE TABLE IF NOT EXISTS sys_tenant (
    id BIGINT NOT NULL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    package_id BIGINT,
    tenant_name VARCHAR(50) NOT NULL,
    tenant_type VARCHAR(10) NOT NULL,
    tenant_path VARCHAR(255),
    contact_user VARCHAR(20),
    contact_phone VARCHAR(11),
    status SMALLINT DEFAULT 0,
    expire_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tenant_parent_id ON sys_tenant (parent_id);
CREATE INDEX idx_tenant_path ON sys_tenant (tenant_path);

-- 3. 菜单权限表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL PRIMARY KEY,
    menu_name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    order_num INT DEFAULT 0,
    path VARCHAR(200) DEFAULT '',
    component VARCHAR(255),
    perms VARCHAR(100),
    menu_type CHAR(1) DEFAULT '',
    icon VARCHAR(100) DEFAULT '#',
    tenant_type VARCHAR(10) DEFAULT 'ALL'
);

-- 4. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL PRIMARY KEY,
    role_name VARCHAR(30) NOT NULL,
    role_key VARCHAR(100) NOT NULL,
    data_scope CHAR(1) DEFAULT '1',
    status SMALLINT NOT NULL,
    tenant_id BIGINT NOT NULL
);
CREATE INDEX idx_role_tenant_id ON sys_role (tenant_id);

-- 5. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(30) NOT NULL,
    nickname VARCHAR(30) DEFAULT '',
    password VARCHAR(100) NOT NULL,
    user_type VARCHAR(10) DEFAULT 'SYS',
    dept_id BIGINT,
    status SMALLINT DEFAULT 0,
    CONSTRAINT uk_username_tenant UNIQUE (username, tenant_id)
);

-- 6. 用户与角色关联
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 7. 角色与菜单关联
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

-- 8. 消费者表
CREATE TABLE IF NOT EXISTS sys_consumer (
    id BIGINT NOT NULL PRIMARY KEY,
    union_id VARCHAR(64),
    phone VARCHAR(11) NOT NULL,
    nickname VARCHAR(30) DEFAULT '',
    avatar VARCHAR(255) DEFAULT '',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_consumer_phone UNIQUE (phone)
);

-- 9. 消费者商家关联表
CREATE TABLE IF NOT EXISTS rel_consumer_tenant (
    consumer_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    member_level INT DEFAULT 0,
    points INT DEFAULT 0,
    PRIMARY KEY (consumer_id, tenant_id)
);

-- 10. 审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT NOT NULL PRIMARY KEY,
    tenant_id BIGINT DEFAULT 0 NOT NULL,
    module VARCHAR(64) NOT NULL DEFAULT '',
    operation VARCHAR(128) NOT NULL DEFAULT '',
    business_id VARCHAR(64) NOT NULL DEFAULT '',
    operator VARCHAR(64) NOT NULL DEFAULT '',
    cost_time BIGINT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_msg CLOB, -- 错误堆栈通常很长，使用 CLOB
    changes JSON, -- H2 支持 JSON 类型定义（内部按字符串处理）
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_tenant_module ON audit_log (tenant_id, module);
CREATE INDEX idx_audit_business_id ON audit_log (business_id);
CREATE INDEX idx_audit_create_time ON audit_log (create_time);