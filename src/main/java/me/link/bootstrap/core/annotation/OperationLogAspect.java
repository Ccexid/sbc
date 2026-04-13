package me.link.bootstrap.core.annotation;

import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.util.SpelUtils;
import me.link.bootstrap.util.SystemClockUtils;
import me.link.bootstrap.util.TraceUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

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
        String traceId = TraceUtils.get();
        log.info("[OperationLogAspect] traceId: {}, method: {}, args: {}", traceId, joinPoint.getSignature(), joinPoint.getArgs());
        long startNano = SystemClockUtils.now();
        Long bizId = parseSpel(joinPoint, operationLog.bizId(), null);
//        Object oldData = captureSnapshot(operationLog, bizId);
        log.info("[OperationLogAspect] traceId: {}, method: {}, args: {}", traceId, joinPoint.getSignature(), bizId);
        return joinPoint.proceed();
    }

    private Long parseSpel(ProceedingJoinPoint joinPoint, String expression, Object result) {
        if (ObjectUtil.isEmpty(expression)) return 0L;
        Map<String, Object> vars = result != null ? Map.of("result", result) : Collections.emptyMap();
        String val = SpelUtils.parseExpression(joinPoint, expression, vars);
        return ObjectUtil.isEmpty(val) ? 0L : Long.parseLong(val);
    }
}
