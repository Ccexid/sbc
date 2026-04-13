package me.link.bootstrap.modules.system.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import me.link.bootstrap.shared.kernel.enums.StatusEnum;
import me.link.bootstrap.shared.infrastructure.mybatis.handler.StringToSetTypeHandler;
import me.link.bootstrap.modules.system.domain.model.valueobject.PlatformEnum;

import java.util.List;

@TableName(value = "system_organization", autoResultMap = true) // 必须开启 autoResultMap 以支持 TypeHandler
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDO extends TenantBasePO {
    /**
     * 主体ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 主体名称 (如：XX房源供应商、XX中介门店)
     */
    private String name;

    /**
     * 主体类型
     * 对应枚举：1:供应商S, 2:平台P, 3:商家B
     */
    private PlatformEnum orgType;

    /**
     * 上级主体 ID
     * 顶级节点的 parent_id 为 0
     */
    private Long parentId;

    /**
     * 层级路径
     * 存储格式如 "0,1,10"，方便通过 LIKE "0,1,%" 快速查询下级所有节点
     */
    @TableField(typeHandler = StringToSetTypeHandler.class)
    private List<String> ancestors;

    /**
     * 层级深度
     * 根节点为 1，子节点递增
     */
    private Integer level;

    /**
     * 负责人姓名
     */
    private String contactName;

    /**
     * 负责人联系电话
     */
    private String contactMobile;

    /**
     * 状态
     * 0:正常, 1:停用
     */
    private StatusEnum status;
}
