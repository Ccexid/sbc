package me.link.bootstrap.shared.infrastructure.web.aspect;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.shared.infrastructure.web.util.SpelUtil;
import me.link.bootstrap.shared.kernel.annotation.OperationLog;
import me.link.bootstrap.shared.util.SystemClockUtil;
import me.link.bootstrap.shared.util.TraceUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

import static me.link.bootstrap.shared.kernel.constant.GlobalConstants.MAX_ERROR_MSG_LEN;

@Aspect
@Component
@Slf4j
@Order(1) // 确保在事务切面之后执行，保证 newData 能查到已提交或已 Flush 的变更
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ApplicationContext applicationContext;
    private final Executor logExecutor;

    @Around("@annotation(operationLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        String traceId = TraceUtil.get();
        log.info("[OperationLogAspect] traceId: {}, method: {}, args: {}", traceId, joinPoint.getSignature(), joinPoint.getArgs());
        long startNano = SystemClockUtil.now();
        Long bizId = parseSpel(joinPoint, operationLog.bizId(), null);
        Object oldData = captureSnapshot(operationLog, bizId);
        log.info("[OperationLogAspect] traceId: {}, method: {}, args: {}", traceId, joinPoint.getSignature(), oldData);
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 异常分支：立即异步记录失败日志
            Long failId = parseSpel(joinPoint, operationLog.bizId(), null);
            throw e;
        }
        Long finalBusinessId = ObjectUtil.isEmpty(bizId) ? parseSpel(joinPoint, operationLog.bizId(), result) : bizId;
        Object newData = captureSnapshot(operationLog, finalBusinessId);
        String dynamicOp = parseSpelStr(joinPoint, operationLog.description(), result);
        return result;
    }

    private Long parseSpel(ProceedingJoinPoint joinPoint, String expression, Object result) {
        if (ObjectUtil.isEmpty(expression)) return 0L;
        Map<String, Object> vars = result != null ? Map.of("result", result) : Collections.emptyMap();
        String val = SpelUtil.parseExpression(joinPoint, expression, vars);
        return ObjectUtil.isEmpty(val) ? 0L : Long.parseLong(val);
    }

    private String parseSpelStr(ProceedingJoinPoint joinPoint, String expression, Object result) {
        if (ObjectUtil.isEmpty(expression)) return "";
        Map<String, Object> vars = result != null ? Map.of("result", result) : Collections.emptyMap();
        String val = SpelUtil.parseExpression(joinPoint, expression, vars);
        return ObjectUtil.isEmpty(val) ? "" : val;
    }

    private Object captureSnapshot(OperationLog anno, Long id) {
        if (ObjectUtil.isEmpty(anno.repository()) || ObjectUtil.isEmpty(id)) return null;
        try {
            Object service = applicationContext.getBean(anno.repository());
            Method method = service.getClass().getMethod("getById", Serializable.class);
            Object rawData = method.invoke(service, id);
            // 深拷贝：脱离持久化上下文（防止 Hibernate/JPA 一级缓存干扰）
            return rawData != null ? BeanUtil.toBean(rawData, rawData.getClass()) : null;
        } catch (Exception ex) {
            log.debug("[OperationLog] 获取快照跳过: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * 构建格式化的错误消息
     * <p>
     * 将异常类型和消息组合成可读的错误描述，并在超过最大长度时进行截断处理，
     * 避免过长的错误信息影响日志存储和展示。
     * </p>
     *
     * @param e 异常对象
     * @return 格式化的错误消息字符串，如果异常为null则返回null
     */
    private String buildErrorMessage(Throwable e) {
        if (e == null) return null;
        String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
        return msg.length() > MAX_ERROR_MSG_LEN ? msg.substring(0, MAX_ERROR_MSG_LEN - 3) + "..." : msg;
    }
}
