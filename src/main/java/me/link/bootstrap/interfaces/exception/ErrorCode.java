package me.link.bootstrap.interfaces.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    SUCCESS(0, "操作成功"),
    USER_NOT_FOUND(401_000_001, "用户不存在"),
    SYSTEM_ERROR(500_000_001, "系统内部错误");
    ;
    private final long code;
    private final String message;
}
