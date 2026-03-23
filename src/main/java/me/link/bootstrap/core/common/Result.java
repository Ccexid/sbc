package me.link.bootstrap.core.common;

import me.link.bootstrap.core.exception.ErrorCode;
import me.link.bootstrap.core.exception.IErrorCode;
import me.link.bootstrap.core.utils.SystemClock;

import java.io.Serializable;

/**
 * 通用响应结果封装类 (基于 Java Record)
 * <p>
 * 该类用于统一后端接口的返回格式，包含状态码、消息、数据负载和时间戳。
 * 使用 Record 特性简化了不可变数据载体的定义，同时提供了便捷的静态工厂方法。
 * </p>
 *
 * @param <T> 响应数据的具体类型
 */
public record Result<T>(
        /**
         * 业务状态码
         * <ul>
         *   <li>200 或特定成功码：表示请求处理成功</li>
         *   <li>其他非零值：表示业务错误或系统异常</li>
         * </ul>
         */
        long code,

        /**
         * 响应消息
         * <ul>
         *   <li>成功时：通常为 "success" 或具体成功提示</li>
         *   <li>失败时：描述错误原因的详细信息</li>
         * </ul>
         */
        String msg,

        /**
         * 响应数据负载
         * <ul>
         *   <li>成功时：包含具体的业务数据对象</li>
         *   <li>失败时：通常为 null</li>
         * </ul>
         */
        T data,

        /**
         * 响应生成的时间戳 (毫秒级)
         * <ul>
         *   <li>用于客户端计算请求耗时或进行缓存校验</li>
         *   <li>如果构造时未提供或非法，将自动设置为当前系统时间</li>
         * </ul>
         */
        long timestamp
) implements Serializable {

    /**
     * 紧凑型构造函数 (Compact Constructor)
     * <p>
     * 作用：在对象实例化过程中执行额外的逻辑校验和默认值填充。
     * 注意：Record 的紧凑构造函数不声明参数列表，直接使用记录组件名称。
     * </p>
     * 步骤说明：
     * 1. 检查传入的 timestamp 是否合法 (大于 0)。
     * 2. 如果非法 (<= 0)，则使用系统时钟获取当前时间作为默认值。
     * 3. 确保最终生成的结果对象一定拥有有效的时间戳。
     */
    public Result {
        if (timestamp <= 0) {
            timestamp = SystemClock.now();
        }
    }

    /**
     * 构建成功的响应结果
     * <p>
     * 作用：快速创建一个状态为成功的 Result 对象。
     * </p>
     * 步骤说明：
     * 1. 获取预定义的成功状态码 (ErrorCode.SUCCESS.getCode())。
     * 2. 获取预定义的成功消息 (ErrorCode.SUCCESS.getMsg())。
     * 3. 将传入的数据对象设置为 data 字段。
     * 4. 自动生成当前系统时间戳。
     * 5. 返回新的 Result 实例。
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 包含成功状态和业务数据的 Result 对象
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(
                ErrorCode.SUCCESS.getCode(),
                ErrorCode.SUCCESS.getMsg(),
                data,
                SystemClock.now()
        );
    }

    /**
     * 构建失败的响应结果 (基于错误码枚举)
     * <p>
     * 作用：根据预定义的错误码枚举快速创建一个状态为失败的 Result 对象。
     * </p>
     * 步骤说明：
     * 1. 从传入的 IErrorCode 枚举中提取错误码 (code)。
     * 2. 从传入的 IErrorCode 枚举中提取错误消息 (msg)。
     * 3. 将 data 字段显式设置为 null，表示无有效数据返回。
     * 4. 自动生成当前系统时间戳。
     * 5. 返回新的 Result 实例。
     *
     * @param errorCode 预定义的错误码枚举
     * @param <T>       数据类型泛型 (此处固定为 null)
     * @return 包含错误状态和错误信息的 Result 对象
     */
    public static <T> Result<T> error(IErrorCode errorCode) {
        return new Result<>(
                errorCode.getCode(),
                errorCode.getMsg(),
                null,
                SystemClock.now()
        );
    }

    /**
     * 构建失败的响应结果 (自定义错误码和消息)
     * <p>
     * 作用：使用自定义的状态码和消息创建一个状态为失败的 Result 对象，适用于动态错误场景。
     * </p>
     * 步骤说明：
     * 1. 直接使用传入的 code 参数作为状态码。
     * 2. 直接使用传入的 msg 参数作为错误消息。
     * 3. 将 data 字段显式设置为 null。
     * 4. 自动生成当前系统时间戳。
     * 5. 返回新的 Result 实例。
     *
     * @param code 自定义错误状态码
     * @param msg  自定义错误消息
     * @param <T>  数据类型泛型 (此处固定为 null)
     * @return 包含自定义错误状态的 Result 对象
     */
    public static <T> Result<T> error(long code, String msg) {
        return new Result<>(
                code,
                msg,
                null,
                SystemClock.now()
        );
    }
}