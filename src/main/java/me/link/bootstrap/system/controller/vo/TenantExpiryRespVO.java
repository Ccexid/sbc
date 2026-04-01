package me.link.bootstrap.system.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import me.link.bootstrap.system.dal.enums.ExpiredEnum;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "管理后台 - 租户过期状态 Response VO")
public class TenantExpiryRespVO {

    @Schema(description = "是否已过期", example = "false")
    private Boolean isExpired;

    @Schema(description = "剩余天数", example = "15")
    private Long remainingDays;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    @Schema(description = "合约状态 1-履约中 2-已到期", example = "1")
    private ExpiredEnum contractStatus;
}
