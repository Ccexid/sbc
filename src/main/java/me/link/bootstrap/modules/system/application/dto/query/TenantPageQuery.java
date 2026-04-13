package me.link.bootstrap.modules.system.application.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.shared.kernel.pojo.SortablePageParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 租户分页查询对象
 * 继承自 SortablePageParam，支持分页、排序及业务过滤
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "租户分页查询参数")
public class TenantPageQuery extends SortablePageParam {

    @Schema(description = "租户名 (模糊查询)", example = "极客")
    private String name;

    @Schema(description = "联系人姓名 (模糊查询)", example = "林")
    private String contactName;

    @Schema(description = "联系手机", example = "13800138000")
    private String contactMobile;

    @Schema(description = "状态 (0:正常, 1:禁用)", example = "0")
    private Integer status;

    @Schema(description = "套餐编号", example = "1")
    private Long packageId;

    @Schema(description = "开始创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginCreateTime;

    @Schema(description = "结束创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endCreateTime;

    @Schema(description = "到期时间筛选 (true: 已过期, false: 未过期)")
    private Boolean isExpired;
}