package me.link.bootstrap.modules.system.domain.model.valueobject;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 到期状态枚举类
 * 用于表示合同或协议的履约状态
 */
@Getter
@AllArgsConstructor
public enum ExpiredEnum {
    /**
     * 履约中状态
     */
    IN_FORCE(1, "履约中"),
    /**
     * 已到期状态
     */
    EXPIRED(2, "已到期");

    /**
     * 状态值
     */
    @EnumValue
    private final Integer value;
    
    /**
     * 状态描述
     */
    @JsonValue
    private final String desc;
}
