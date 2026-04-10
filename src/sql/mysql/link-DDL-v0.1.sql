DROP TABLE IF EXISTS `system_sequence`;

CREATE TABLE `system_sequence` (
                                   `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `biz_code` varchar(64) NOT NULL COMMENT '业务标识(如: ORDER, TENANT)',
                                   `current_value` bigint NOT NULL DEFAULT '0' COMMENT '当前最大序列号',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `uk_biz_code` (`biz_code`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分布式序列号维护表';

DROP TABLE IF EXISTS `system_tenant_package`;

CREATE TABLE `system_tenant_package` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '套餐编号',
                                         `name` varchar(30) NOT NULL COMMENT '套餐名',
                                         `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
                                         `remark` varchar(256) DEFAULT '' COMMENT '备注',
                                         `menu_ids` json NOT NULL COMMENT '关联的菜单编号数组',
                                         `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删 1已删)',
                                         PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户套餐表';

DROP TABLE IF EXISTS `system_tenant`;

CREATE TABLE `system_tenant` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户编号',
                                 `name` varchar(64) NOT NULL COMMENT '租户名',
                                 `contact_user_id` bigint DEFAULT NULL COMMENT '联系人的用户编号',
                                 `contact_name` varchar(32) NOT NULL COMMENT '联系人',
                                 `contact_mobile` varchar(128) DEFAULT NULL COMMENT '联系手机(加密存储)',
                                 `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
                                 `websites` json DEFAULT NULL COMMENT '绑定域名数组',
                                 `package_id` bigint NOT NULL COMMENT '套餐编号',
                                 `expire_time` datetime NOT NULL COMMENT '过期时间',
                                 `account_count` int NOT NULL DEFAULT 0 COMMENT '账号数量限制',
                                 `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                 PRIMARY KEY (`id`),
    -- 增加索引提高查询效率
                                 KEY `idx_package_id` (`package_id`),
                                 KEY `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户表';

DROP TABLE IF EXISTS `system_users`;

CREATE TABLE `system_users` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                                `username` varchar(30) NOT NULL COMMENT '用户账号',
                                `password` varchar(100) NOT NULL DEFAULT '' COMMENT '密码',
                                `nickname` varchar(30) NOT NULL COMMENT '用户昵称',
                                `user_type` tinyint NOT NULL DEFAULT 2 COMMENT '身份类型（1:供应商S, 2:平台P, 3:商家B, 4:用户C）',
                                `mobile` varchar(20) NOT NULL COMMENT '手机号码',
                                `avatar` varchar(512) DEFAULT '' COMMENT '头像',
                                `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
                                `org_id` bigint DEFAULT NULL COMMENT '所属组织ID（对应供应商ID或商家ID）',
                                `dept_id` bigint DEFAULT NULL COMMENT '平台内部部门ID（仅P端使用）',
                                `login_ip` varchar(50) DEFAULT '' COMMENT '最后登录IP',
                                `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
                                `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                PRIMARY KEY (`id`),
                                UNIQUE INDEX `uk_username_tenant` (`username`, `tenant_id`),
                                UNIQUE INDEX `uk_mobile_type` (`mobile`, `user_type`),
                                INDEX `idx_org_id` (`org_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

DROP TABLE IF EXISTS `system_organization`;

CREATE TABLE `system_organization` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主体ID',
                                       `name` varchar(100) NOT NULL COMMENT '主体名称',
                                       `org_type` tinyint NOT NULL COMMENT '主体类型 (1:供应商S, 2:平台P, 3:商家B)',
                                       `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '上级主体 (B的上级通常是P，S由P管理或独立)',
                                       `ancestors` varchar(512) DEFAULT '' COMMENT '层级路径 (用于快速检索，如 0,2,10)',
                                       `level` tinyint DEFAULT 1 COMMENT '层级深度',
                                       `contact_name` varchar(30) DEFAULT '' COMMENT '负责人',
                                       `contact_mobile` varchar(20) DEFAULT '' COMMENT '联系电话',
                                       `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
                                       `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                       `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                       PRIMARY KEY (`id`),
                                       INDEX `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '组织表';

DROP TABLE IF EXISTS `system_role`;

CREATE TABLE `system_role` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                               `name` varchar(30) NOT NULL COMMENT '角色名称',
                               `code` varchar(100) NOT NULL COMMENT '角色权限字符串',
                               `sort` int NOT NULL COMMENT '显示顺序',
                               `data_scope` tinyint NOT NULL DEFAULT 1 COMMENT '数据范围（1:全部, 2:自定义, 3:本组织, 4:本组织及以下）',
                               `data_scope_dept_ids` varchar(500) NOT NULL DEFAULT '' COMMENT '数据范围(指定组织/部门数组)',
                               `status` tinyint NOT NULL DEFAULT 0 COMMENT '角色状态（0正常 1停用）',
                               `type` tinyint NOT NULL COMMENT '角色类型（1:系统内置 2:自定义）',
                               `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                               `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                               `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                               PRIMARY KEY (`id`),
                               INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色信息表';

DROP TABLE IF EXISTS `system_menu`;

CREATE TABLE `system_menu` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
                               `name` varchar(50) NOT NULL COMMENT '菜单名称',
                               `permission` varchar(100) NOT NULL DEFAULT '' COMMENT '权限标识',
                               `type` tinyint NOT NULL COMMENT '菜单类型（1:目录 2:菜单 3:按钮）',
                               `sort` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
                               `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID',
                               `path` varchar(200) DEFAULT '' COMMENT '路由地址',
                               `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
                               `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
                               `component_name` varchar(255) DEFAULT NULL COMMENT '组件名',
                               `status` tinyint NOT NULL DEFAULT 0 COMMENT '菜单状态（0:正常 1:停用）',
                               `visible` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否可见（0:显示 1:隐藏）',
                               `keep_alive` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否缓存（0:开启 1:关闭）',
                               `always_show` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否总是显示（0:是 1:否）',
                               `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                               PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表';

DROP TABLE IF EXISTS `system_role_menu`;

CREATE TABLE `system_role_menu` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增编号',
                                    `role_id` bigint NOT NULL COMMENT '角色ID',
                                    `menu_id` bigint NOT NULL COMMENT '菜单ID',
                                    `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                    PRIMARY KEY (`id`),
                                    INDEX `idx_role_id` (`role_id`),
                                    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色和菜单关联表';

DROP TABLE IF EXISTS `system_user_role`;

CREATE TABLE `system_user_role` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增编号',
                                    `user_id` bigint NOT NULL COMMENT '用户ID',
                                    `role_id` bigint NOT NULL COMMENT '角色ID',
                                    `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                    PRIMARY KEY (`id`),
                                    INDEX `idx_user_id` (`user_id`),
                                    INDEX `idx_role_id` (`role_id`),
                                    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户和角色关联表';

DROP TABLE IF EXISTS `system_operate_log`;

CREATE TABLE `system_operate_log` (
                                      `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增编号',
                                      `trace_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪编号',
                                      `user_id` bigint UNSIGNED NOT NULL COMMENT '用户编号',
                                      `user_type` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户类型',
                                      `user_ip` varchar(45) DEFAULT '' COMMENT '用户 IP(支持 IPv6)',
                                      `user_agent` varchar(512) DEFAULT '' COMMENT '浏览器 UA',
                                      `module` varchar(255) NOT NULL COMMENT '操作模块(对应 type)',
                                      `operation` tinyint UNSIGNED NOT NULL COMMENT '操作 1-CREATED 2-UPDATED 3-DELETED 4-RETRIEVED',
                                      `biz_id` bigint NOT NULL COMMENT '业务主键编号',
                                      `action` text NOT NULL COMMENT '操作内容/变更详情',
                                      `extra` json DEFAULT NULL COMMENT '拓展字段(改用 JSON 类型)',
                                      `success` tinyint(1) NOT NULL DEFAULT 1 COMMENT '操作结果(1:成功, 0:失败)',
                                      `request_method` varchar(10) DEFAULT '' COMMENT '请求方法名',
                                      `request_url` varchar(512) DEFAULT '' COMMENT '请求地址',
                                      `duration` int UNSIGNED DEFAULT 0 COMMENT '执行耗时(ms)',
                                      `creator` bigint DEFAULT NULL COMMENT '创建者ID',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updater` bigint DEFAULT NULL COMMENT '更新者ID',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
                                      `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      INDEX `idx_tenant_biz` (`tenant_id`, `biz_id`) USING BTREE,
                                      INDEX `idx_create_time` (`create_time`) USING BTREE,
                                      INDEX `idx_user_id` (`user_id`) USING BTREE,
                                      INDEX `idx_trace_id` (`trace_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志记录';