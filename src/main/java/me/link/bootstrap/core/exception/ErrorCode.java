package me.link.bootstrap.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 * 格式：项目(1位)-模块(3位)-类型(3位)-序号(3位)
 */
@Getter
@AllArgsConstructor
public enum ErrorCode implements IErrorCode {

    // --- 1-001-000-xxx 通用基础异常 ---
    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(1001000001L, "系统繁忙，请稍后再试"),
    PARAM_ERROR(1001000002L, "参数校验失败"),
    REPEATED_REQUESTS(1001000003L, "请求过于频繁"),
    BAD_REQUEST(1001000004L, "请求参数错误"),

    // --- 1-001-100-xxx 认证权限异常 ---
    UNAUTHORIZED(1001100001L, "暂未登录或Token已过期"),
    FORBIDDEN(1001100002L, "权限不足，拒绝访问"),
    TENANT_NOT_FOUND(1001100003L, "租户信息不存在"),

    // --- 1-001-200-xxx 数据层异常 ---
    DATA_NOT_FOUND(1001200001L, "数据不存在或已删除"),
    DATA_EXISTED(1001200002L, "数据已存在，请勿重复创建"),
    CACHE_PENETRATION(1001200003L, "非法请求拦截（布隆过滤器命中）"),

    // --- 1-001-300-xxx 业务逻辑异常 ---
    USER_FROZEN(1001300001L, "当前账号已被冻结"),
    OPERATION_FAILED(1001300002L, "业务处理失败");

    private final long code;
    private final String msg;
}