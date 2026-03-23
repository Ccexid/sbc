package me.link.bootstrap.core.tenant.aspect;

import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.tenant.annotation.IgnoreTenant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 租户忽略切面
 * 用于在标记了 @IgnoreTenant 的类或方法执行时，临时忽略租户上下文检查
 */
@Aspect
@Component
@Slf4j
@Order(0) // 确保在事务拦截器之前执行，避免事务提交后无法修改上下文
public class IgnoreTenantAspect {

    /**
     * 环绕通知：拦截带有 @IgnoreTenant 注解的方法或类
     *
     * @param joinPoint 切点对象，用于获取方法签名和执行目标方法
     * @param ignoreTenant 注解实例，用于确认触发条件
     * @return 目标方法的执行结果
     * @throws Throwable 执行过程中可能抛出的异常
     */
    @Around("@within(ignoreTenant) || @annotation(ignoreTenant)")
    public Object around(ProceedingJoinPoint joinPoint, IgnoreTenant ignoreTenant) throws Throwable {
        // 步骤1: 备份当前线程的租户忽略状态，以支持嵌套调用场景
        // 如果外部已经忽略了租户，内部再次忽略时不会丢失外部状态
        boolean oldState = TenantContextHolder.isIgnore();
        try {
            // 步骤2: 记录日志，追踪哪些方法触发了租户忽略逻辑
            log.trace("[Tenant] 开启动态忽略租户：{}", joinPoint.getSignature());
            
            // 步骤3: 设置当前线程忽略租户检查
            // 此操作会影响当前线程后续的所有租户相关逻辑，直到被还原
            TenantContextHolder.setIgnore(true);
            
            // 步骤4: 执行目标方法
            // 此时目标方法及其调用的子方法都将运行在“忽略租户”的状态下
            return joinPoint.proceed();
        } finally {
            // 步骤5: 无论方法执行成功还是抛出异常，都必须还原之前的租户忽略状态
            // 防止 ThreadLocal 变量污染导致后续请求出现错误的租户隔离行为
            TenantContextHolder.setIgnore(oldState);
        }
    }
}