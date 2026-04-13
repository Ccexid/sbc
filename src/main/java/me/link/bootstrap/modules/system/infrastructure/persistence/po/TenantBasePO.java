package me.link.bootstrap.modules.system.infrastructure.persistence.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.shared.infrastructure.mybatis.domain.BaseDO;
import me.link.bootstrap.shared.kernel.annotation.LogField;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class TenantBasePO extends BaseDO {
    @Serial
    private final static long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    @LogField(value = "租户编号")
    private Long tenantId;
}
