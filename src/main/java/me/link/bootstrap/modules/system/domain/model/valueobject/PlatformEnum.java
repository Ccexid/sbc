package me.link.bootstrap.modules.system.domain.model.valueobject;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlatformEnum {
    /**
     * 供应商 (Supplier) - 提供房源/货源的源头
     */
    SUPPLIER(1, "供应商S"),

    /**
     * 平台方 (Platform) - 运营主体，拥有管理权限
     */
    PLATFORM(2, "平台P"),

    /**
     * 商家 (Business) - 分销商或服务商
     */
    BUSINESS(3, "商家B"),

    /**
     * 消费者 (Consumer) - 最终用户/租客
     */
    CONSUMER(4, "用户C");

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
