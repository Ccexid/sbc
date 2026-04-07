package me.link.bootstrap.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class TenantBaseDO extends BaseDO {
    @Serial
    private final static long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    private Long tenantId;
}
