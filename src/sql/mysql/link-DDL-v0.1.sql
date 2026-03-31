CREATE TABLE `system_sequence`
(
    `id`            bigint      NOT NULL COMMENT '主键ID',
    `name`          varchar(64) NOT NULL COMMENT '业务标识(如: FD, PD:20260330)',
    `current_value` bigint      NOT NULL DEFAULT '0' COMMENT '当前最大序列号',
    `version`       bigint      NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
    `create_time`   datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`) -- 核心：保证业务标识唯一
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分布式序列号维护表';