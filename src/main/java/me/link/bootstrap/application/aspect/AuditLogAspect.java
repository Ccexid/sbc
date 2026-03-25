package me.link.bootstrap.application.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.domain.log.model.AuditLogDTO;
import me.link.bootstrap.domain.log.model.FieldChangeDetail;
import me.link.bootstrap.domain.log.spi.AuditLogStorage;
import me.link.bootstrap.infrastructure.annotation.Log;
import me.link.bootstrap.infrastructure.context.SpringContextHolder;
import me.link.bootstrap.infrastructure.context.TenantContextHolder;
import me.link.bootstrap.infrastructure.utils.BeanDiffUtils;
import me.link.bootstrap.infrastructure.utils.SpelUtils;
import me.link.bootstrap.infrastructure.utils.TraceUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 审计日志切面 (高性能 & 异步多租户版本)
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditLogAspect {

    private final Executor logExecutor;
    private final List<AuditLogStorage> storageProviders; // 自动注入所有实现类

    @Around("@annotation(auditLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, Log auditLog) throws Throwable {
        // 1. 提取主线程上下文 (防止异步丢失)
        String tenantId = TenantContextHolder.getTenantId();
        String traceId = TraceUtils.getTraceId();
        long startTime = System.currentTimeMillis();

        // 2. 初始解析业务 ID
        String businessId = SpelUtils.parse(joinPoint, auditLog.businessId(), null);

        // 3. 前置数据快照 (仅在非新增操作且开启 Diff 时获取)
        Object oldData = null;
        if (auditLog.isDiff() && ObjectUtil.isNotEmpty(businessId) && !"N/A".equals(businessId)) {
            oldData = captureDataSnapshot(auditLog.serviceName(), businessId);
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 异常记录：此时无法获取 result，解析 operation 时变量设为 null
            handleLog(auditLog, joinPoint, businessId, oldData, null, startTime, tenantId, traceId, e);
            throw e;
        }

        // 4. 后置处理：获取最终 BusinessId (支持新增场景从返回值获取 ID)
        String finalBusinessId = businessId;
        if (ObjectUtil.isEmpty(businessId) || "N/A".equals(businessId)) {
            finalBusinessId = SpelUtils.parse(joinPoint, auditLog.businessId(), Map.of("result", result));
        }

        // 5. 后置数据快照 (仅在开启 Diff 时获取)
        Object newData = auditLog.isDiff() ? captureDataSnapshot(auditLog.serviceName(), finalBusinessId) : null;

        // 6. 正常记录
        handleLog(auditLog, joinPoint, finalBusinessId, oldData, newData, startTime, tenantId, traceId, null);

        return result;
    }

    private void handleLog(Log anno, ProceedingJoinPoint jp, String bId, Object oldD, Object newD,
                           long start, String tenantId, String traceId, Throwable ex) {

        // 解析操作描述 (支持 #result)
        Map<String, Object> vars = ex == null ? Map.of("result", "FAILED") : Map.of(); // 简单处理，实际可更复杂
        String op = SpelUtils.parse(jp, anno.operation(), vars);

        // 异步执行保存逻辑
        logExecutor.execute(() -> {
            try {
                // 还原租户上下文 (针对异步线程)
                TenantContextHolder.setTenantId(tenantId);
                TraceUtils.setTraceId(traceId);

                // 计算差异
                List<FieldChangeDetail> changes = (anno.isDiff() && oldD != null && newD != null)
                        ? BeanDiffUtils.diff(oldD, newD) : null;

                // 构建 DTO
                AuditLogDTO dto = AuditLogDTO.builder()
                        .tenantId(ObjectUtil.isEmpty(tenantId) ? 0L : Long.parseLong(tenantId))
                        .traceId(traceId)
                        .module(anno.module())
                        .operation(op)
                        .businessId(bId)
                        .costTime(System.currentTimeMillis() - start)
                        .status(ex == null ? "SUCCESS" : "FAIL")
                        .errorMsg(ex != null ? ex.getMessage() : null)
                        .changes(changes)
                        .createTime(LocalDateTime.now())
                        .build();

                // 多源存储
                storageProviders.stream()
                        // 1. 过滤掉未启用的存储实现（例如开发环境下禁用了 ES 存储）
                        .filter(AuditLogStorage::isEnabled)
                        // 2. 按优先级排序（如果有多个存储，可以先存 DB，再发 MQ）
                        .sorted(Comparator.comparingInt(AuditLogStorage::getOrder))
                        .forEach(storage -> {
                            try {
                                // 3. 执行真正的保存逻辑
                                storage.record(dto);

                                // 4. (可选) 调试模式下记录哪个存储器保存成功
                                log.debug("[AuditLog] 存储器 [{}] 保存成功, TraceId: {}", storage.getName(), dto.getTraceId());
                            } catch (Exception var11) {
                                // 5. 核心：异常隔离。
                                // 某个存储器（如 ES）挂了，不能影响其他存储器（如 DB）的正常写入
                                log.error("[AuditLog] 存储器 [{}] 保存异常, 原因: {}", storage.getName(), var11.getMessage());
                            }
                        });

            } catch (Exception e) {
                log.error("[AuditLog] 异步记录审计日志失败", e);
            } finally {
                // 必须清理，防止异步线程污染
                TenantContextHolder.clear();
                TraceUtils.clear();
            }
        });
    }

    /**
     * 获取数据快照 (使用 SpringContextHolder 降低耦合)
     */
    private Object captureDataSnapshot(String serviceName, String id) {
        if (ObjectUtil.isEmpty(serviceName) || ObjectUtil.isEmpty(id)) return null;
        try {
            Object service = SpringContextHolder.getBean(serviceName);
            // 约定 Service 必须提供 getById 方法
            Method method = service.getClass().getMethod("getById", Serializable.class);
            Object data = method.invoke(service, id);
            // 深拷贝：防止后续逻辑修改内存对象导致 Diff 为空
            return data != null ? BeanUtil.toBean(data, data.getClass()) : null;
        } catch (Exception e) {
            log.debug("[AuditLog] 快照获取跳过: Service[{}], ID[{}]", serviceName, id);
            return null;
        }
    }
}