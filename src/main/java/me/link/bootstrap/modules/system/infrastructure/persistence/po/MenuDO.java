package me.link.bootstrap.modules.system.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import me.link.bootstrap.shared.kernel.enums.StatusEnum;
import me.link.bootstrap.modules.system.domain.model.valueobject.MenuTypeEnum;

/**
 * 系统菜单数据对象
 * 对应数据库表 system_menu，用于存储系统菜单、目录和按钮的权限配置信息
 *
 * @author Link System
 * @since 1.0
 */
@TableName(value = "system_menu", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MenuDO extends TenantBasePO {

    /**
     * 菜单ID
     * 主键，自增
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 权限标识
     * 用于前端权限控制，如 system:user:list
     */
    private String permission;

    /**
     * 菜单类型
     * 1:目录 2:菜单 3:按钮
     */
    private MenuTypeEnum type;

    /**
     * 排序号
     * 数值越小越靠前
     */
    private Integer sort;

    /**
     * 父菜单ID
     * 顶级菜单为 null 或 0
     */
    private Long parentId;

    /**
     * 路由路径
     * 前端路由匹配的 URL 路径
     */
    private String path;

    /**
     * 菜单图标
     * 支持 Element Plus 图标名称或自定义图标类名
     */
    private String icon;

    /**
     * 组件路径
     * 前端组件文件的相对路径，如 /system/user/index
     */
    private String component;

    /**
     * 组件名称
     * 前端组件的名称标识，用于 keep-alive 缓存
     */
    private String componentName;

    /**
     * 菜单状态
     * 启用或禁用该菜单
     */
    private StatusEnum status;

    /**
     * 是否可见
     * false 时菜单不在侧边栏显示，但仍可访问
     */
    private Boolean visible;

    /**
     * 是否缓存
     * true 时使用 keep-alive 缓存组件状态
     */
    private Boolean keepAlive;

    /**
     * 是否始终显示
     * true 时即使只有一个子菜单也显示父菜单
     */
    private Boolean alwaysShow;
}
