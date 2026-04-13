package me.link.bootstrap.modules.system.application.dto.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import me.link.bootstrap.modules.system.domain.model.valueobject.ExpiredEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static me.link.bootstrap.shared.kernel.constants.GlobalConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@Builder
@Schema(description = "管理后台 - 租户过期状态 Response VO")
public class TenantExpiryRespVO {

    @Schema(description = "是否已过期", example = "false")
    private Boolean isExpired;

    @Schema(description = "剩余天数", example = "15")
    private Long remainingDays;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;

    @Schema(description = "合约状态 1-履约中 2-已到期", example = "1")
    private ExpiredEnum contractStatus;
}
