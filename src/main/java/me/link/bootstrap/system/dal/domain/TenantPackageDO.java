package me.link.bootstrap.system.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.link.bootstrap.core.domain.BaseDO;
import me.link.bootstrap.core.enums.StatusEnum;

import java.io.Serial;
import java.util.Set;

/**
 * 租户套餐表 DO
 *
 * @author Gemini
 */
@TableName(value = "system_tenant_package", autoResultMap = true) // 必须开启 autoResultMap 以支持 TypeHandler
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantPackageDO extends BaseDO {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐名
     */
    private String name;

    /**
     * 租户状态
     * 对应枚举：0-正常 1-停用
     */
    private StatusEnum status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联的菜单编号
     * 数据库存储格式为 JSON 字符串: [1, 2, 5, ...]
     * 使用 JacksonTypeHandler 自动实现 String <-> Set<Long> 的转换
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<Long> menuIds;

}
