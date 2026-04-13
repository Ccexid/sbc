package me.link.bootstrap.modules.system.application.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.link.bootstrap.shared.kernel.enums.StatusEnum;
import me.link.bootstrap.shared.kernel.pojo.SortablePageParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static me.link.bootstrap.shared.kernel.constant.GlobalConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 租户分页查询对象
 * 继承自 SortablePageParam，支持分页、排序及业务过滤
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "租户分页查询参数")
public class TenantPageQuery extends SortablePageParam {

    @Schema(description = "租户名 (模糊查询)", example = "极客")
    @Size(max = 64, message = "租户名称长度不能超过 64 个字符")
    private String name;

    @Schema(description = "联系人姓名 (模糊查询)", example = "林")
    @Size(max = 32, message = "联系人姓名长度不能超过 32 个字符")
    private String contactName;

    @Schema(description = "联系手机", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactMobile;

    @Schema(description = "状态 (0:正常, 1:禁用)", example = "0")
    private StatusEnum status;

    @Schema(description = "套餐编号", example = "1")
    private Long packageId;

    @Schema(description = "开始创建时间")
    @DateTimeFormat(pattern =  FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime beginCreateTime;

    @Schema(description = "结束创建时间")
    @DateTimeFormat(pattern =  FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endCreateTime;

    @Schema(description = "到期时间筛选 (true: 已过期, false: 未过期)")
    private Boolean isExpired;
}