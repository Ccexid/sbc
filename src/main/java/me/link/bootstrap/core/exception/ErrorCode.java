package me.link.bootstrap.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 * 格式：项目(1位) - 模块(3位) - 类型(3位) - 序号(3)
 * 示例：1 001 000 001
 */
@Getter
@AllArgsConstructor
public enum ErrorCode implements IErrorCode {

    // ========== 1-001-000-xxx 通用基础异常 ==========
    SUCCESS(0, "操作成功"),
    SYSTEM_ERROR(1001000001L, "系统繁忙，请稍后再试"),
    PARAM_ERROR(1001000002L, "参数校验失败"),
    REPEATED_REQUESTS(1001000003L, "请求过于频繁"),
    BAD_REQUEST(1001000004L, "非法请求"),
    METHOD_NOT_ALLOWED(1001000005L, "不支持的请求方式"),
    DEMO_MODE(1001000006L, "演示模式，禁止写操作"),

    // ========== 1-001-100-xxx 认证权限异常 (核心适配 Sa-Token) ==========
    UNAUTHORIZED(1001100001L, "暂未登录或会话已过期"),
    FORBIDDEN(1001100002L, "权限不足，拒绝访问"),
    TOKEN_INVALID(1001100003L, "Token 无效或已黑名单"),
    LOGIN_EXPIRED(1001100004L, "登录已过期，请重新登录"),
    ACCOUNT_LOCKED(1001100005L, "账号已被锁定"),
    SECOND_AUTH_REQUIRED(1001100006L, "需要二级认证"),

    // ========== 1-001-200-xxx 租户异常 (P2S2B2C 专项补充) ==========
    TENANT_NOT_FOUND(1001200001L, "租户信息不存在"),
    TENANT_FORBIDDEN(1001200002L, "租户已被禁用"),
    TENANT_EXPIRED(1001200003L, "租户服务已到期"),
    TENANT_QUOTA_FULL(1001200004L, "租户账号额度已满"),
    TENANT_CROSS_ERROR(1001200005L, "禁止跨租户非法操作数据"),
    TENANT_PACKAGE_NOT_FOUND(1001200006L, "租户未关联套餐"),

    // ========== 1-001-300-xxx 数据层异常 ==========
    DATA_NOT_FOUND(1001300001L, "请求的数据不存在"),
    DATA_EXISTED(1001300002L, "数据已存在，请勿重复创建"),
    DATA_UPDATE_FAIL(1001300003L, "数据更新失败"),
    DATA_DELETE_FAIL(1001300004L, "数据包含关联引用，禁止删除"),

    // ========== 1-001-400-xxx 业务逻辑异常 ==========
    OPERATION_FAILED(1001400001L, "业务处理失败"),
    FILE_UPLOAD_ERROR(1001400002L, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORT(1001400003L, "不支持的文件类型"),
    EXPORT_TOO_MANY(1001400004L, "导出数据量过大，请增加筛选条件");

    private final long code;
    private final String msg;
}