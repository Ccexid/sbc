package me.link.bootstrap.domain.log.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.link.bootstrap.infrastructure.enums.LogType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据传输对象 (DTO)
 * 优化点：增强元数据记录、支持请求/响应镜像、标准化序列化
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审计日志数据传输对象，涵盖全链路审计核心要素")
public class AuditLogDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户 ID", example = "1")
    private Long tenantId;

    @Schema(description = "追踪 ID (MDC TraceId)", example = "a1b2c3d4e5f6")
    private String traceId;

    @Schema(description = "业务模块", example = "用户管理")
    private String module;

    @Schema(description = "操作类型", example = "UPDATE")
    private LogType logType;

    @Schema(description = "操作描述 (SpEL 解析后)", example = "修改用户[张三]的状态为[禁用]")
    private String operation;

    @Schema(description = "业务主键 ID", example = "10086")
    private String businessId;

    @Schema(description = "请求方法", example = "me.link.controller.UserController.updateStatus")
    private String method;

    @Schema(description = "请求路径", example = "/api/v1/user/status")
    private String requestUrl;

    @Schema(description = "操作人 ID", example = "500")
    private String operatorId;

    @Schema(description = "操作人账号", example = "admin")
    private String operatorName;

    @Schema(description = "操作 IP 地址", example = "192.168.1.100")
    private String operatorIp;

    @Schema(description = "操作地点", example = "上海-浦东")
    private String operatorLocation;

    @Schema(description = "请求参数 (JSON 字符串)", example = "{\"id\":10086, \"status\":0}")
    private String requestParam;

    @Schema(description = "响应结果 (JSON 字符串)", example = "{\"code\":200, \"msg\":\"ok\"}")
    private String responseData;

    @Schema(description = "执行耗时 (ms)", example = "156")
    private Long costTime;

    @Schema(description = "执行状态", example = "SUCCESS")
    private String status;

    @Schema(description = "异常堆栈摘要", example = "UserNotFoundException: 用户不存在")
    private String errorMsg;

    @Schema(description = "记录时间")
    private LocalDateTime createTime;

    @Schema(description = "数据 Diff 详情")
    private List<FieldChangeDetail> changes;
}