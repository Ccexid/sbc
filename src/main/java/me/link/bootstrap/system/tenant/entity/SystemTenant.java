package me.link.bootstrap.system.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.core.common.BaseEntity;

import java.time.LocalDateTime;

/**
 * 系统租户实体 (P2S2B2C 架构)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
@Schema(description = "系统租户信息")
public class SystemTenant extends BaseEntity {

    @Schema(description = "上级租户 ID (0表示顶级)")
    private Long parentId;

    @Schema(description = "关联套餐 ID")
    private Long packageId;

    @Schema(description = "租户名称")
    private String tenantName;

    /**
     * 租户类型：P(平台), S(服务商), B(商家)
     */
    @Schema(description = "租户类型：P(平台), S(服务商), B(商家)")
    private String tenantType;

    /**
     * 租户链路溯源 (例如：0,1,10,105)
     * 作用：用于快速查询某个服务商下属的所有商家
     */
    @Schema(description = "租户链路溯源")
    private String tenantPath;

    @Schema(description = "联系人")
    private String contactUser;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "租户状态（0 正常 1 停用）")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
}