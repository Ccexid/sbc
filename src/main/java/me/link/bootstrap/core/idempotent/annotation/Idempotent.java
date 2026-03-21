package me.link.bootstrap.core.idempotent.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /** 幂等提示消息 */
    String message() default "请勿重复提交";

    /** 强制过期时间（秒），防止 Token 长期堆积 */
    long expireTime() default 3600;

    /** Header 中 Token 的名称 */
    String headerName() default "X-Idempotent-Token";
}