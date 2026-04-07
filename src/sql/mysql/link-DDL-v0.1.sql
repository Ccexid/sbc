DROP TABLE IF EXISTS `system_sequence`;
CREATE TABLE `system_sequence`
(
    `id`            bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_code`      varchar(64) NOT NULL COMMENT '业务标识(如: ORDER, TENANT)',
    `current_value` bigint      NOT NULL DEFAULT '0' COMMENT '当前最大序列号',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_code` (`biz_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分布式序列号维护表';
DROP TABLE IF EXISTS `system_tenant_package`;
CREATE TABLE `system_tenant_package`
(
    `id`          bigint UNSIGNED NOT NULL COMMENT '套餐编号',
    `name`        varchar(30) NOT NULL COMMENT '套餐名',
    `status`      tinyint     NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `remark`      varchar(256)         DEFAULT '' COMMENT '备注',
    `menu_ids`    json        NOT NULL COMMENT '关联的菜单编号数组',
    `creator`     bigint                DEFAULT NULL COMMENT '创建者ID',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater`     bigint                DEFAULT NULL COMMENT '更新者ID',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     tinyint (1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0未删 1已删)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户套餐表';

DROP TABLE IF EXISTS `system_tenant`;
CREATE TABLE `system_tenant`
(
    `id`              bigint UNSIGNED NOT NULL COMMENT '租户编号',
    `name`            varchar(64) NOT NULL COMMENT '租户名',
    `contact_user_id` bigint               DEFAULT NULL COMMENT '联系人的用户编号',
    `contact_name`    varchar(32) NOT NULL COMMENT '联系人',
    `contact_mobile`  varchar(128)         DEFAULT NULL COMMENT '联系手机(加密存储)',
    `status`          tinyint     NOT NULL DEFAULT 0 COMMENT '状态',
    `websites`        json                 DEFAULT NULL COMMENT '绑定域名数组',
    `package_id`      bigint      NOT NULL COMMENT '套餐编号',
    `expire_time`     datetime    NOT NULL COMMENT '过期时间',
    `account_count`   int         NOT NULL DEFAULT 0 COMMENT '账号数量限制',
    `creator`     bigint                DEFAULT NULL COMMENT '创建者ID',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater`     bigint                DEFAULT NULL COMMENT '更新者ID',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         tinyint (1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    -- 增加索引提高查询效率
    KEY               `idx_package_id` (`package_id`),
    KEY               `idx_expire_time` (`expire_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户表';

DROP TABLE IF EXISTS `system_users`;
CREATE TABLE `system_users`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    varchar(30)  NOT NULL COMMENT '用户账号',
    `password`    varchar(100) NOT NULL DEFAULT '' COMMENT '密码',
    `nickname`    varchar(30)  NOT NULL COMMENT '用户昵称',
    `user_type`   tinyint      NOT NULL DEFAULT 2 COMMENT '身份类型（1:供应商S, 2:平台P, 3:商家B, 4:用户C）',
    `mobile`      varchar(20)  NOT NULL COMMENT '手机号码',
    `avatar`      varchar(512)          DEFAULT '' COMMENT '头像',
    `status`      tinyint      NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
    `org_id`      bigint                DEFAULT NULL COMMENT '所属组织ID（对应供应商ID或商家ID）',
    `dept_id`     bigint                DEFAULT NULL COMMENT '平台内部部门ID（仅P端使用）',
    `login_ip`    varchar(50)           DEFAULT '' COMMENT '最后登录IP',
    `login_date`  datetime              DEFAULT NULL COMMENT '最后登录时间',
    `creator`     bigint                DEFAULT NULL COMMENT '创建者ID',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater`     bigint                DEFAULT NULL COMMENT '更新者ID',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     tinyint (1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id`   bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username_tenant` (`username`, `tenant_id`),
    UNIQUE INDEX `uk_mobile_type` (`mobile`, `user_type`),
    INDEX         `idx_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

DROP TABLE IF EXISTS `system_organization`;
CREATE TABLE `system_organization`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主体ID',
    `name`           varchar(100) NOT NULL COMMENT '主体名称',
    `org_type`       tinyint      NOT NULL COMMENT '主体类型 (1:供应商S, 2:平台P, 3:商家B)',
    `parent_id`      bigint       NOT NULL DEFAULT 0 COMMENT '上级主体 (B的上级通常是P，S由P管理或独立)',
    `ancestors`      varchar(512)          DEFAULT '' COMMENT '层级路径 (用于快速检索，如 0,2,10)',
    `level`          tinyint               DEFAULT 1 COMMENT '层级深度',
    `contact_name`   varchar(30)           DEFAULT '' COMMENT '负责人',
    `contact_mobile` varchar(20)           DEFAULT '' COMMENT '联系电话',
    `status`         tinyint      NOT NULL DEFAULT 0 COMMENT '状态',
    `creator`        bigint                DEFAULT NULL COMMENT '创建者ID',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater`        bigint                DEFAULT NULL COMMENT '更新者ID',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        tinyint (1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `tenant_id`      bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX            `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB COMMENT='组织表';