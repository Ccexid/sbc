package me.link.bootstrap.modules.system.application.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户视图对象 - 用于前端列表及详情展示
 */
@Data
@Schema(description = "租户信息展示对象")
public class TenantVO {

    @Schema(description = "租户编号", example = "1024")
    private Long id;

    @Schema(description = "租户名", example = "极客链接有限公司")
    private String name;

    @Schema(description = "联系人姓名", example = "林某某")
    private String contactName;

    @Schema(description = "联系手机 (脱敏展示)", example = "138****8888")
    private String contactMobile;

    @Schema(description = "租户状态 (0:正常, 1:禁用)", example = "0")
    private Integer status;

    @Schema(description = "绑定域名", example = "[\"me.link.io\", \"api.link.io\"]")
    private List<String> websites;

    @Schema(description = "套餐编号", example = "1")
    private Long packageId;

    @Schema(description = "套餐名称 (通过关联查询或缓存获取)", example = "尊享版套餐")
    private String packageName;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "账号数量限制", example = "100")
    private Integer accountCount;

    @Schema(description = "当前已使用账号数", example = "42")
    private Integer currentAccountCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // --- 派生展示字段 (UI 友好型) ---

    @Schema(description = "是否已过期", example = "false")
    private Boolean isExpired;

    @Schema(description = "剩余天数", example = "365")
    private Long remainingDays;
}