package me.link.bootstrap.system.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.core.common.PageReq;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "租户分页查询请求")
public class TenantPageReqVO extends PageReq {

    @Schema(description = "租户名称，模糊匹配")
    private String tenantName;

    @Schema(description = "联系人名称，模糊匹配")
    private String contactUser;

    @Schema(description = "租户类型：P/S/B")
    private String tenantType;

    @Schema(description = "状态 (0正常 1停用)")
    private Integer status;

}
