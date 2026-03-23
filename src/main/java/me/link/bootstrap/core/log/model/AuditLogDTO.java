package me.link.bootstrap.core.log.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据传输对象 (DTO)
 * 用于记录和传递系统操作审计信息
 */
@Data
@Builder
@Schema(description = "审计日志数据传输对象，用于记录和传递系统操作审计信息")
public class AuditLogDTO {

    /**
     * 租户唯一标识
     */
    @Schema(description = "租户唯一标识", example = "tenant_001")
    private String tenantId;

    /**
     * 业务模块名称
     */
    @Schema(description = "业务模块名称", example = "UserModule")
    private String module;

    /**
     * 具体操作描述
     */
    @Schema(description = "具体操作描述", example = "Create User")
    private String operation;

    /**
     * 关联的业务主键 ID
     */
    @Schema(description = "关联的业务主键 ID", example = "1234567890")
    private String businessId;

    /**
     * 请求追踪 ID (TraceId)，用于链路追踪
     */
    @Schema(description = "请求追踪 ID (TraceId)，用于链路追踪", example = "abc-123-def-456")
    private String requestId;

    /**
     * 操作人标识（如用户名或用户 ID）
     */
    @Schema(description = "操作人标识（如用户名或用户 ID）", example = "admin")
    private String operator;

    /**
     * 接口耗时（单位：毫秒）
     */
    @Schema(description = "接口耗时（单位：毫秒）", example = "150")
    private String costTime;

    /**
     * 操作执行状态 (SUCCESS: 成功，FAIL: 失败)
     */
    @Schema(description = "操作执行状态", example = "SUCCESS", allowableValues = {"SUCCESS", "FAIL"})
    private String status;

    /**
     * 错误信息简述或异常堆栈摘要
     */
    @Schema(description = "错误信息简述或异常堆栈摘要", example = "NullPointerException at line 42")
    private String errorMsg;

    /**
     * 日志记录时间
     */
    @Schema(description = "日志记录时间")
    private LocalDateTime createTime;

    /**
     * 数据变更明细列表
     */
    @Schema(description = "数据变更明细列表")
    private List<FieldChangeDetail> changes;
}