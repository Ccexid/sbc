package me.link.bootstrap.core.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseTenantEntity extends BaseEntity {

    /**
     * 租户 ID
     * fill = FieldFill.INSERT 表示仅在插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
