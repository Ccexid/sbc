package me.link.bootstrap.core.common;

import me.link.bootstrap.core.exception.IErrorCode;
import me.link.bootstrap.core.utils.SystemClock;

import java.io.Serializable;

/**
 * 通用响应结果封装类
 * 使用 Java Record 特性，自动包含构造函数、getter、equals、hashCode 和 toString 方法
 * @param <T> 响应数据的泛型类型
 */
public record Result<T>(
        // 状态码：0 表示成功，非 0 表示失败或特定业务状态
        long code,
        // 响应消息：描述操作结果或错误信息
        String msg,
        // 响应数据：承载具体的业务数据，失败时通常为 null
        T data,
        // 时间戳：记录响应生成的时间（毫秒级）
        long timestamp
) implements Serializable {

    /**
     * 构建成功的响应结果
     * 步骤：
     * 1. 设置状态码为 0，代表操作成功
     * 2. 设置消息为 "SUCCESS"
     * 3. 填入用户传入的业务数据
     * 4. 使用系统时钟获取当前时间戳
     *
     * @param data 业务数据
     * @param <T> 数据类型
     * @return 包含成功信息的 Result 对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "SUCCESS", data, SystemClock.now());
    }

    /**
     * 构建失败的响应结果（基于错误码枚举）
     * 步骤：
     * 1. 从传入的错误码枚举中获取具体的状态码
     * 2. 从传入的错误码枚举中获取对应的错误消息
     * 3. 将数据部分设为 null，表示无有效业务数据返回
     * 4. 使用系统时钟获取当前时间戳
     *
     * @param errorCode 预定义的错误码枚举接口
     * @param <T> 数据类型（此处固定为 null，但保留泛型以匹配类定义）
     * @return 包含错误信息的 Result 对象
     */
    public static <T> Result<T> error(IErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMsg(), null, SystemClock.now());
    }

    /**
     * 构建失败的响应结果（基于自定义状态码和消息）
     * 步骤：
     * 1. 直接使用传入的状态码
     * 2. 直接使用传入的错误消息
     * 3. 将数据部分设为 null
     * 4. 使用系统时钟获取当前时间戳
     *
     * @param code 自定义状态码
     * @param msg 自定义错误消息
     * @param <T> 数据类型（此处固定为 null，但保留泛型以匹配类定义）
     * @return 包含错误信息的 Result 对象
     */
    public static <T> Result<T> error(long code, String msg) {
        return new Result<>(code, msg, null, SystemClock.now());
    }
}