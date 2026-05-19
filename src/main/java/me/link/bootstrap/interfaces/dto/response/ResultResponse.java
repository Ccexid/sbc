package me.link.bootstrap.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.link.bootstrap.infrastructure.tracing.TraceIdContext;
import me.link.bootstrap.interfaces.exception.ErrorCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Schema(description = "统一响应结果")
@Data
public class ResultResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "业务数据", example = "{\"id\": 123, \"name\": \"example\"}")
    private T data;
    @Schema(description = "用户可读提示消息", example = "操作成功")
    private String message;

    @Schema(description = "业务错误码（非HTTP状态码）", example = "401_000_001")
    private Long code;

    @Schema(description = "服务端处理完成时间戳（ISO 8601）", example = "2026-05-19T15:46:22.123Z")
    private Instant timestamp;

    @Schema(description = "链路追踪ID（用于日志关联）", example = "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8")
    private String traceId;


    private ResultResponse() {
    }

    private ResultResponse(final T data, final String message, final Long code, final Instant timestamp, final String traceId) {
        this.data = data;
        this.message = message;
        this.code = code;
        this.timestamp = timestamp;
        this.traceId = traceId;
    }

    private static <T> ResultResponse<T> of(final T data, final String message, final Long code) {
        return new ResultResponse<>(null, message, code, Instant.now(), TraceIdContext.get());
    }

    /**
     * 创建成功响应结果
     *
     * @param data 响应数据，泛型类型，可以是任意对象
     * @param <T>  数据类型泛型参数
     * @return 包含数据和成功状态码(200)的ResultResponse对象
     */
    public static <T> ResultResponse<T> success(T data) {
        return of(data, ErrorCode.SUCCESS.getMessage(), ErrorCode.SUCCESS.getCode());
    }

    public static <T> ResultResponse<T> success() {
        return of(null, ErrorCode.SUCCESS.getMessage(), ErrorCode.SUCCESS.getCode());
    }

    public static <T> ResultResponse<T> success(String message) {
        return of(null, message, ErrorCode.SUCCESS.getCode());
    }

    public static <T> ResultResponse<T> success(T data, String message) {
        return of(data, message, ErrorCode.SUCCESS.getCode());
    }

    public static <T> ResultResponse<T> success(T data, String message, Long code) {
        return of(data, message, code);
    }


    public static <T> ResultResponse<T> failure(ErrorCode errorCode) {
        return of(null, errorCode.getMessage(), errorCode.getCode());
    }

    public static <T> ResultResponse<T> failure(ErrorCode errorCode, String message) {
        return of(null, message, errorCode.getCode());
    }
}
