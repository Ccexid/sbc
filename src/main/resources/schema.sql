CREATE TABLE IF NOT EXISTS "system_sequence"
(
    "id"
    BIGINT
    NOT
    NULL,
    "name"
    VARCHAR
(
    64
) NOT NULL,
    "current_value" BIGINT NOT NULL DEFAULT 0,
    "version" BIGINT NOT NULL DEFAULT 0,
    "create_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "update_time" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY
(
    "id"
),
    CONSTRAINT "uk_name" UNIQUE
(
    "name"
)
    );

-- H2 不支持在建表语句内直接通过 COMMENT 关键字给字段加注释的 MySQL 语法
-- 建议使用单独的语句（可选）
COMMENT
ON TABLE "system_sequence" IS '分布式序列号维护表';
COMMENT
ON COLUMN "system_sequence"."id" IS '主键ID';
COMMENT
ON COLUMN "system_sequence"."name" IS '业务标识';