package me.link.bootstrap.modules.system.domain.model.valueobject;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 菜单类型枚举
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    /**
     * 目录 (Directory)
     * 通常作为一级或多级菜单的容器，没有实际的 component
     */
    DIR(1, "目录"),

    /**
     * 菜单 (Menu)
     * 对应具体的业务页面，配置有具体的组件路径 (component)
     */
    MENU(2, "菜单"),

    /**
     * 按钮 (Button)
     * 页面内的操作权限标识（如：system:user:add），不参与路由渲染
     */
    BUTTON(3, "按钮");

    /**
     * 类型值
     */
    @EnumValue
    private final Integer value;

    /**
     * 类型描述
     */
    @JsonValue
    private final String desc;

    /**
     * 根据值获取枚举对象
     */
    public static MenuTypeEnum valueOf(Integer value) {
        return Arrays.stream(values())
                .filter(item -> item.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断是否为目录
     */
    public boolean isDir() {
        return DIR.getValue().equals(this.value);
    }

    /**
     * 判断是否为菜单
     */
    public boolean isMenu() {
        return MENU.getValue().equals(this.value);
    }

    /**
     * 判断是否为按钮
     */
    public boolean isButton() {
        return BUTTON.getValue().equals(this.value);
    }
}