package me.link.bootstrap.core.annotation;

import java.lang.annotation.*;

/**
 * 审计日志字段标记注解
 * 作用：标记在 Entity 或 DTO 的字段上，用于差异对比时显示字段名称
 */
@Target(ElementType.FIELD) // 只能标记在字段上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效，供反射读取
@Documented
public @interface LogField {
    /**
     * 字段描述名称
     * 例如：@LogField("用户姓名")
     */
    String value() default "";
}