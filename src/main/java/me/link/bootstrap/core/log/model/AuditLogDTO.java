package me.link.bootstrap.core.log.model;

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
public class AuditLogDTO {

    /**
     * 租户唯一标识
     */
    private String tenantId;

    /**
     * 业务模块名称
     */
    private String module;

    /**
     * 具体操作描述
     */
    private String operation;

    /**
     * 关联的业务主键 ID
     */
    private String businessId;

    /**
     * 请求追踪 ID (TraceId)，用于链路追踪
     */
    private String requestId;

    /**
     * 操作人标识（如用户名或用户 ID）
     */
    private String operator;

    /**
     * 接口耗时（单位：毫秒）
     */
    private String costTime;

    /**
     * 操作执行状态 (SUCCESS: 成功, FAIL: 失败)
     */
    private String status;

    /**
     * 错误信息简述或异常堆栈摘要
     */
    private String errorMsg;

    /**
     * 日志记录时间
     */
    private LocalDateTime createTime;

    /**
     * 数据变更明细列表
     */
    private List<FieldChangeDetail> changes;
}