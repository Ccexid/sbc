package me.link.bootstrap.core.exception;

import lombok.Getter;

/**
 * 全局业务异常类
 * <p>
 * 支持错误码与错误消息的组合，用于统一处理业务层面的异常情况。
 * </p>
 */
@Getter
public class GlobalException extends RuntimeException {

    /**
     * 业务错误码
     */
    private final Integer code;

    /**
     * 构造全局异常（默认错误码为 500）
     *
     * @param message 异常消息
     */
    public GlobalException(String message) {
        this(500, message);
    }

    /**
     * 构造全局异常（默认错误码为 500）
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public GlobalException(String message, Throwable cause) {
        this(500, message, cause);
    }

    /**
     * 构造全局异常（指定错误码）
     *
     * @param code    业务错误码
     * @param message 异常消息
     */
    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造全局异常（指定错误码）
     *
     * @param code    业务错误码
     * @param message 异常消息
     * @param cause   原始异常
     */
    public GlobalException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
