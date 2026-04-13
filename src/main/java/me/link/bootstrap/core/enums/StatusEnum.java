package me.link.bootstrap.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举类
 * 用于定义系统中的各种状态，支持 MyBatis Plus 枚举类型自动映射
 */
@Getter
@AllArgsConstructor
public enum StatusEnum implements IEnum<Integer> {
    /**
     * 正常状态
     */
    NORMAL(0, "正常"),
    /**
     * 停用状态
     */
    DISABLE(1, "停用");

    /**
     * 状态值（存储到数据库）
     */
    @EnumValue // 标记数据库存的值
    private final Integer value;
    
    /**
     * 状态描述（返回给前端）
     */
    @JsonValue
    private final String desc;

    /**
     * 获取状态值
     * MyBatis Plus 接口实现方法，用于将枚举转换为数据库存储的整数值
     *
     * @return 状态值，对应数据库中的整数类型字段
     */
    @Override
    public Integer getValue() {
        return this.value;
    }
}
