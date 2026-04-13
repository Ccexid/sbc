package me.link.bootstrap.modules.system.domain.model.valueobject;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据范围枚举
 * 对应 system_role 表中的 data_scope 字段
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    /**
     * 全部数据权限
     */
    ALL(1, "全部数据权限"),

    /**
     * 指定组织数据权限 (自定义)
     * 配合 system_role 中的 data_scope_dept_ids 使用
     */
    DEPT_CUSTOM(2, "自定数据权限"),

    /**
     * 本组织数据权限
     */
    DEPT_ONLY(3, "本部门数据权限"),

    /**
     * 本组织及以下数据权限
     * 在 S2P2B2C 树形架构中，通常用于 P 端查看下属所有 B 端数据
     */
    DEPT_AND_CHILD(4, "本部门及以下数据权限"),

    /**
     * 仅本人数据权限 (预留)
     */
    SELF(5, "仅本人数据权限");

    /**
     * 数据库存储值
     */
    @EnumValue
    private final Integer value;

    /**
     * 前端展示描述
     */
    @JsonValue
    private final String desc;
}