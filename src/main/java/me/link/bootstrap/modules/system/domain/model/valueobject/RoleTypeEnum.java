package me.link.bootstrap.modules.system.domain.model.valueobject;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleTypeEnum {

    /**
     * 系统内置角色
     * 由平台（P端）统一预设，租户不可删除或修改关键编码（Code）
     */
    SYSTEM(1, "系统内置"),

    /**
     * 自定义角色
     * 由租户（S端或B端）管理员自行创建，用于精细化分配员工权限
     */
    CUSTOM(2, "自定义");

    @EnumValue
    private final Integer value;
    @JsonValue
    private final String desc;
}
