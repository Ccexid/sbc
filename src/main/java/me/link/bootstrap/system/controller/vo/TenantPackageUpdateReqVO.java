package me.link.bootstrap.system.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.core.pojo.FieldMaskParam;
import me.link.bootstrap.system.dal.domain.TenantPackageDO;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "租户套餐更新操作继承对象")
@Data
public class TenantPackageUpdateReqVO extends FieldMaskParam {
    @Schema(description = "待更新的套餐信息 (仅需填充 Mask 指定的字段)")
    TenantPackageDO tenantPackage;
}
