package me.link.bootstrap.core.log.aspect;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.log.annotation.Log;
import me.link.bootstrap.core.log.model.AuditLogDTO;
import me.link.bootstrap.core.log.model.FieldChangeDetail;
import me.link.bootstrap.core.log.spi.AuditLogStorageProvider;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.utils.BeanDiffUtils;
import me.link.bootstrap.core.utils.SystemClock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 高性能审计日志切面 (JDK 17 优化版)
 * 核心职责：拦截 @Log 注解，异步对比数据差异并分发至各存储介质
 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect implements ApplicationContextAware {

    /**
     * Spring 应用上下文，用于动态获取 Bean（如 Service）
     */
    private ApplicationContext applicationContext;

    /**
     * SpEL 表达式解析器，用于解析注解中定义的业务 ID 表达式
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 专用异步线程池执行器，确保日志保存操作不阻塞主业务线程
     * 该线程池已装饰，可自动传递租户上下文（TenantId）和区域信息（Locale）
     */
    private final Executor auditLogExecutor;

    /**
     * 构造注入：通过 Qualifier 指定使用名为 "auditLogExecutor" 的线程池
     * 目的：确保使用的线程池支持上下文传递，避免异步丢失租户信息
     */
    public AuditLogAspect(@Qualifier("auditLogExecutor") Executor auditLogExecutor) {
        this.auditLogExecutor = auditLogExecutor;
    }

    /**
     * 设置 ApplicationContext，由 Spring 容器自动调用
     * @param applicationContext Spring 应用上下文实例
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 环绕通知：核心拦截逻辑
     * 拦截所有标记了 @Log 注解的方法，执行前后进行数据快照捕获和差异记录
     *
     * @param joinPoint 连接点，包含方法执行的相关信息
     * @param auditLog 方法上的 @Log 注解实例，包含配置信息
     * @return 原始方法的返回值
     * @throws Throwable 可能抛出的任何异常
     */
    @Around("@annotation(auditLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log auditLog) throws Throwable {
        // 步骤 1: 解析 SpEL 表达式获取业务唯一标识 (businessId)
        // 作用：从方法参数或上下文中动态提取需要审计的业务对象 ID，例如 "#user.id"
        String businessId = parseSpel(joinPoint, auditLog.businessId());

        // 步骤 2: 记录开始时间 (纳秒级)
        // 作用：使用 SystemClock 获取当前时间，用于记录方法执行开始时间
        long startNano = SystemClock.now();

        // 步骤 3: 在业务执行前同步捕获旧数据快照
        // 作用：必须在业务修改数据之前获取旧状态，用于后续对比差异；内部已做异常保护，失败不影响主流程
        Object oldData = captureOldData(auditLog, businessId);

        Object result;
        try {
            // 步骤 4: 执行原始业务方法
            // 作用：继续执行被拦截的目标方法，完成实际业务逻辑
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 步骤 5: 捕获业务异常，触发异步日志记录（状态为 FAIL）
            // 作用：即使业务失败，也要记录审计日志，包含错误信息和当时的数据状态
            asyncSave(auditLog, businessId, oldData, null, startNano, e);
            // 重新抛出异常，保持原有业务异常行为
            throw e;
        }

        // 步骤 6: 获取新数据
        // 作用：默认取方法第一个参数作为新数据（通常为更新后的 DTO 或 Entity），用于与旧数据对比
        // 注意：此处假设第一个参数即为变更后的数据，若需更灵活策略可调整
        Object newData = joinPoint.getArgs().length > 0 ? joinPoint.getArgs()[0] : null;

        // 步骤 7: 业务成功后，异步保存数据差异日志
        // 作用：将新旧数据差异、执行耗时、状态等信息封装后，提交到异步线程池进行持久化，不阻塞主线程
        asyncSave(auditLog, businessId, oldData, newData, startNano, null);

        // 返回原始方法的执行结果
        return result;
    }

    /**
     * 捕获旧数据快照
     * 作用：根据注解配置的 serviceName 和 businessId，反射调用对应 Service 的 getById 方法获取旧数据
     *      并使用 Hutool 进行深拷贝，防止后续业务逻辑修改原对象导致快照失效
     *
     * @param auditLog @Log 注解实例，包含 serviceName 等配置
     * @param businessId 业务唯一标识
     * @return 深拷贝后的旧数据对象，若获取失败则返回 null
     */
    private Object captureOldData(Log auditLog, String businessId) {
        // 若未配置服务名或业务 ID 无效，则无需获取旧数据
        if (auditLog.serviceName().isEmpty() || "N/A".equals(businessId)) {
            return null;
        }
        try {
            // 通过反射获取旧数据原始对象
            Object rawData = fetchOldData(auditLog.serviceName(), businessId);
            // 使用 Hutool 工具类进行深拷贝，确保快照独立性
            return rawData != null ? BeanUtil.toBean(rawData, rawData.getClass()) : null;
        } catch (Exception ex) {
            // 记录警告日志，但不中断主业务流程
            log.warn("[AuditLog] 获取旧数据失败：{}, 业务 ID: {}", auditLog.serviceName(), businessId);
            return null;
        }
    }

    /**
     * 异步保存审计日志
     * 作用：将数据差异、执行耗时、状态等信息封装成 DTO，通过 SPI 机制分发给多个存储实现（如数据库、消息队列等）
     *      整个过程在独立线程中执行，避免影响主业务响应时间
     *
     * @param anno @Log 注解实例，包含模块、操作类型等元数据
     * @param bId 业务唯一标识
     * @param oldD 旧数据快照
     * @param newD 新数据
     * @param startNano 开始时间戳（纳秒）
     * @param e 业务执行过程中抛出的异常（若无则为 null）
     */
    private void asyncSave(Log anno, String bId, Object oldD, Object newD, long startNano, Throwable e) {
        // 提交任务到专用线程池，该线程池已装饰，可自动继承主线程的 TenantId 和 Locale 上下文
        auditLogExecutor.execute(() -> {
            try {
                // 计算业务方法执行耗时（单位：毫秒）
                long costTimeMs = (System.nanoTime() - startNano) / 1_000_000;

                // 对比新旧数据，生成字段变更详情列表
                // 仅当新旧数据均存在时才进行对比，否则设为 null
                List<FieldChangeDetail> changes = (oldD != null && newD != null)
                        ? BeanDiffUtils.diff(oldD, newD)
                        : null;

                // 构建审计日志数据传输对象 (DTO)
                // 包含租户 ID、模块、操作、业务 ID、耗时、状态、错误信息、变更详情、创建时间等
                AuditLogDTO logDTO = AuditLogDTO.builder()
                        .tenantId(TenantContextHolder.getTenantId()) // 从上下文获取当前租户 ID
                        .module(anno.module())                       // 注解定义的模块名称
                        .operation(anno.operation())                 // 注解定义的操作类型
                        .businessId(bId)                             // 解析得到的业务 ID
                        .costTime(costTimeMs + "ms")                 // 格式化耗时
                        .status(e == null ? "SUCCESS" : "FAIL")      // 根据是否有异常设置状态
                        .errorMsg(e != null ? e.getMessage() : null) // 若有异常，记录错误消息
                        .changes(changes)                            // 字段变更详情
                        .createTime(LocalDateTime.now())             // 使用 JDK 17+ 的 LocalDateTime 记录创建时间
                        .build();

                // 遍历所有已注册的存储提供者（SPI 机制），将日志保存到不同介质
                // 使用缓存的实例列表，避免重复加载，提升性能
                AuditLogStorageProvider.getStorages().forEach(storage -> {
                    try {
                        storage.save(logDTO); // 调用具体存储实现的 save 方法
                    } catch (Exception ex) {
                        // 单个存储失败不影响其他存储，记录错误日志
                        log.error("[AuditLog] SPI 存储执行异常：{}", storage.getClass().getSimpleName(), ex);
                    }
                });

            } catch (Exception ex) {
                // 捕获异步处理过程中的任何意外异常，防止线程池任务静默失败
                log.error("[AuditLog] 异步处理流程异常", ex);
            }
        });
    }

    /**
     * 通过反射调用指定 Service 的 getById 方法获取旧数据
     * 作用：根据 serviceName 从 Spring 容器中获取 Bean，并调用其 getById(Serializable id) 方法
     *
     * @param serviceName Spring Bean 的名称（即 Service 的 beanName）
     * @param id 业务 ID
     * @return 查询到的旧数据对象
     * @throws Exception 反射调用过程中可能抛出的异常
     */
    private Object fetchOldData(String serviceName, String id) throws Exception {
        // 从 Spring 应用上下文中获取指定名称的 Service Bean
        Object service = applicationContext.getBean(serviceName);
        // 获取 Service 类中的 getById 方法，参数类型为 Serializable
        Method method = service.getClass().getMethod("getById", Serializable.class);
        // 反射调用该方法，传入业务 ID，返回查询结果
        return method.invoke(service, id);
    }

    /**
     * 解析 SpEL (Spring Expression Language) 表达式
     * 作用：将注解中定义的表达式（如 "#user.id"）解析为实际值，通常用于动态获取业务 ID
     *      支持访问方法参数、上下文变量等
     *
     * @param joinPoint AOP 连接点，提供方法签名和参数信息
     * @param spel SpEL 表达式字符串
     * @return 解析后的字符串值，若解析失败或表达式为空则返回 "N/A"
     */
    private String parseSpel(ProceedingJoinPoint joinPoint, String spel) {
        // 若表达式为空或空白，直接返回 "N/A"
        if (spel == null || spel.isBlank()) return "N/A";

        try {
            // 获取方法签名，用于提取参数名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 创建 SpEL 评估上下文
            EvaluationContext context = new StandardEvaluationContext();

            // 获取方法实际参数
            Object[] args = joinPoint.getArgs();
            // 获取方法参数名称数组
            String[] paramNames = signature.getParameterNames();

            // 将参数名和参数值绑定到上下文中，供 SpEL 表达式使用
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            // 解析并计算表达式值
            Object value = parser.parseExpression(spel).getValue(context);
            // 返回值的字符串形式，若为 null 则返回 "N/A"
            return value != null ? value.toString() : "N/A";
        } catch (Exception e) {
            // 记录调试级别日志，表达式解析失败不影响主流程，返回默认值
            log.debug("[AuditLog] SpEL 解析失败：{}", spel);
            return "N/A";
        }
    }
}