package me.link.bootstrap.core.log.aspect;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.log.annotation.Log;
import me.link.bootstrap.core.log.model.AuditLogDTO;
import me.link.bootstrap.core.log.model.FieldChangeDetail;
import me.link.bootstrap.core.log.spi.AuditLogStorageProvider;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.utils.BeanDiffUtils;
import me.link.bootstrap.core.utils.SpELUtils;
import me.link.bootstrap.core.utils.SystemClock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 审计日志切面
 * 核心功能：拦截标注 @Log 注解的方法，自动记录操作前后的数据变更、执行耗时及状态。
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ApplicationContext applicationContext;
    // SpEL 表达式解析器，用于动态解析注解中的业务 ID 和操作描述
    private final ExpressionParser parser = new SpelExpressionParser();
    // 异步线程池，确保日志保存不阻塞主业务流程
    private final Executor auditLogExecutor;

    /**
     * 环绕通知：核心拦截逻辑
     *
     * 执行流程详解：
     * 1. 【前置准备】解析初始业务 ID：尝试从方法参数中解析 businessId。如果是新增操作，此时可能为 "N/A"。
     * 2. 【前置快照】捕获旧数据：在业务方法执行前，根据 ServiceName 和 BusinessId 从数据库查询并深拷贝一份旧数据（Old Data）。
     * 3. 【执行目标】执行业务逻辑：调用 joinPoint.proceed() 执行实际的业务代码。
     *    - 若发生异常：动态解析操作描述，记录失败日志（包含旧数据，无新数据），并抛出异常。
     * 4. 【后置修正】重新解析业务 ID：如果是新增场景（第一步解析失败），利用返回值（result）再次解析获取自增生成的 ID。
     * 5. 【后置快照】捕获新数据：业务执行成功后，再次从数据库查询最新的数据状态作为新数据（New Data）。
     * 6. 【动态描述】解析操作内容：支持引用 #result 变量，生成最终的操作描述（如 "创建用户:张三"）。
     * 7. 【异步存储】构建日志对象：对比新旧数据生成变更详情（Diff），异步提交到存储提供者。
     */
    @Around("@annotation(auditLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log auditLog) throws Throwable {
        // 步骤 1: 初始解析 BusinessId (主要依赖方法入参)
        String businessId = parseSpEL(joinPoint, auditLog.businessId(), null);
        long startNano = SystemClock.now();

        // 步骤 2: 获取执行前的数据快照 (旧数据)
        // 作用：用于后续与执行后的数据进行对比，计算字段变更详情
        Object oldData = captureDataSnapshot(auditLog.serviceName(), businessId);

        Object result;
        try {
            // 步骤 3: 执行实际的业务方法
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 异常处理分支：
            // 业务执行失败，仅需记录失败状态。此时没有新数据，但需要保留旧数据以便追溯。
            // 注意：此时无法引用 #result，因此解析 operation 时传入 null
            String op = parseSpEL(joinPoint, auditLog.operation(), null);
            asyncSave(auditLog, op, businessId, oldData, null, startNano, e);
            throw e;
        }

        // 步骤 4: 二次解析 BusinessId
        // 作用：处理“新增”场景。新增时入参往往没有 ID，需等待业务执行完后，从返回值 (#result) 中获取生成的 ID
        if ("N/A".equals(businessId) || businessId.isBlank()) {
            businessId = parseSpEL(joinPoint, auditLog.businessId(), result);
        }

        // 步骤 5: 获取执行后的数据快照 (新数据)
        // 作用：获取业务变更后的真实数据状态，用于与 oldData 进行 Diff 对比
        Object newData = captureDataSnapshot(auditLog.serviceName(), businessId);

        // 步骤 6: 动态解析操作描述 (Operation)
        // 作用：支持在注解中使用 SpEL 引用返回值 (如 #result.name)，生成更人性化的操作描述
        String dynamicOp = parseSpEL(joinPoint, auditLog.operation(), result);

        // 步骤 7: 异步构建并保存审计日志
        // 作用：将耗时、状态、变更详情等信息封装，通过线程池异步写入，避免影响主接口响应速度
        asyncSave(auditLog, dynamicOp, businessId, oldData, newData, startNano, null);

        return result;
    }

    /**
     * 捕获数据快照
     * 作用：通过反射调用指定 Service 的 getById 方法，获取当前数据库中的实体对象，并进行深拷贝。
     * 深拷贝目的：防止后续业务逻辑修改了原对象引用，导致 Diff 对比时出现错误（新旧数据指向同一内存地址）。
     *
     * @param serviceName Spring Bean 名称 (如 "userService")
     * @param id 业务主键
     * @return 深拷贝后的数据对象，若获取失败返回 null
     */
    private Object captureDataSnapshot(String serviceName, String id) {
        if (serviceName.isEmpty() || "N/A".equals(id) || id.isBlank()) return null;
        try {
            Object service = applicationContext.getBean(serviceName);
            // 约定：目标 Service 必须实现 getById(Serializable id) 方法
            Method method = service.getClass().getMethod("getById", Serializable.class);
            Object rawData = method.invoke(service, id);
            // 使用 Hutool 进行深拷贝转换
            return rawData != null ? BeanUtil.toBean(rawData, rawData.getClass()) : null;
        } catch (Exception ex) {
            log.warn("[AuditLog] 无法获取数据快照，Service: {}, ID: {}", serviceName, id);
            return null;
        }
    }

    /**
     * 异步保存审计日志
     * 作用：在独立线程中执行日志构建与存储，确保主业务流程的低延迟。
     *
     * @param anno 注解实例，包含模块、是否开启 Diff 等配置
     * @param op 解析后的操作描述
     * @param bId 业务 ID
     * @param oldD 旧数据快照
     * @param newD 新数据快照
     * @param start 开始时间戳
     * @param e 业务执行过程中的异常信息（若无则为 null）
     */
    private void asyncSave(Log anno, String op, String bId, Object oldD, Object newD, long start, Throwable e) {
        auditLogExecutor.execute(() -> {
            try {
                // 计算耗时 (毫秒)
                long costTimeMs = (SystemClock.now() - start) / 1_000;

                // 执行数据差异对比 (仅在开启 Diff 且新旧数据均存在时)
                List<FieldChangeDetail> changes = (anno.isDiff() && oldD != null && newD != null)
                        ? BeanDiffUtils.diff(oldD, newD)
                        : null;

                // 构建日志数据传输对象 (DTO)
                AuditLogDTO logDTO = AuditLogDTO.builder()
                        .tenantId(TenantContextHolder.getTenantId()) // 填充租户上下文
                        .module(anno.module())                       // 模块名称
                        .operation(op)                               // 操作描述
                        .businessId(bId)                             // 业务主键
                        .costTime(costTimeMs + "ms")                 // 执行耗时
                        .status(e == null ? "SUCCESS" : "FAIL")      // 执行状态
                        .errorMsg(e != null ? e.getMessage() : null) // 异常信息
                        .changes(changes)                            // 字段变更详情
                        .createTime(LocalDateTime.now())             // 记录时间
                        .build();

                // 遍历所有注册的存储提供者进行保存 (支持多端存储)
                AuditLogStorageProvider.getStorages().forEach(s -> s.save(logDTO));
            } catch (Exception ex) {
                // 捕获异步线程内的异常，防止吞掉主线程异常或导致线程池故障，仅记录错误日志
                log.error("[AuditLog] 异步保存失败", ex);
            }
        });
    }

    /**
     * 解析 SpEL 表达式
     * 作用：将注解中定义的字符串表达式（如 "#userId", "#result.name"）解析为具体的值。
     * 支持绑定的变量：
     * 1. 方法的所有入参（通过参数名绑定）
     * 2. 方法的返回值（通过变量名 #result 绑定，仅在 result 不为 null 时）
     *
     * @param joinPoint 切点对象，用于获取参数和方法签名
     * @param spel 待解析的表达式字符串
     * @param result 方法执行后的返回值
     * @return 解析后的字符串值；若解析失败或表达式为空，返回 "N/A" 或原字符串
     */
    private String parseSpEL(ProceedingJoinPoint joinPoint, String spel, Object result) {
        if (spel == null || spel.isBlank()) return "N/A";

        // 使用工具类，传入可选的 #result 变量
        Map<String, Object> variables = result != null ? Map.of("result", result) : null;
        String value = SpELUtils.parse(joinPoint, spel, variables);

        return value.isEmpty() ? "N/A" : value;
    }
}