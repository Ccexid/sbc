package me.link.bootstrap.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.core.annotation.LogField;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class TenantBaseDO extends BaseDO {
    @Serial
    private final static long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    @LogField(value = "租户编号")
    private Long tenantId;
}
