package me.link.bootstrap.core.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {
    /** 锁的 Key，支持 SpEL 表达式 */
    String key();

    /** 锁的等待时间，默认 5 秒 */
    long waitTime() default 5;

    /** 锁的自动释放时间，默认 10 秒 */
    long leaseTime() default 10;

    /** 时间单位 */
    TimeUnit unit() default TimeUnit.SECONDS;

    /** 锁的前缀 */
    String prefix() default "lock:";
}