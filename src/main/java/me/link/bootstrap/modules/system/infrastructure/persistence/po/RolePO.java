package me.link.bootstrap.modules.system.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import me.link.bootstrap.shared.kernel.enums.StatusEnum;
import me.link.bootstrap.shared.infrastructure.mybatis.handler.StringToSetTypeHandler;
import me.link.bootstrap.modules.system.domain.model.valueobject.DataScopeEnum;
import me.link.bootstrap.modules.system.domain.model.valueobject.RoleTypeEnum;

import java.util.Set;

@TableName(value = "system_role", autoResultMap = true) // 必须开启 autoResultMap 以支持 TypeHandler
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RolePO extends TenantBasePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String code;

    private Integer sort;

    /**
     * 数据范围
     * 1:全部数据权限 2:自定数据权限 3:本部门数据权限 4:本部门及以下数据权限
     */
    private DataScopeEnum dataScope;

    /**
     * 数据范围(指定部门数组)
     */
    @TableField(typeHandler = StringToSetTypeHandler.class)
    private Set<String> dataScopeDeptIds;

    private StatusEnum status;

    /**
     * 角色类型
     * 1:系统内置 2:自定义
     */
    private RoleTypeEnum type;

    private String remark;
}
