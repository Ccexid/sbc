package me.link.bootstrap.core.lock.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.exception.BusinessException;
import me.link.bootstrap.core.exception.ErrorCode;
import me.link.bootstrap.core.lock.annotation.Lock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(lockAnno)")
    public Object around(ProceedingJoinPoint joinPoint, Lock lockAnno) throws Throwable {
        // 1. 解析 SpEL 表达式获取动态 Key
        String lockKey = parseKey(lockAnno.key(), joinPoint);
        String fullKey = lockAnno.prefix() + lockKey;

        // 2. 获取 Redisson 公平锁（或普通锁）
        RLock lock = redissonClient.getLock(fullKey);

        log.debug("[分布式锁] 尝试获取锁: {}", fullKey);

        // 3. 尝试加锁
        boolean isLocked = lock.tryLock(lockAnno.waitTime(), lockAnno.leaseTime(), lockAnno.unit());

        if (!isLocked) {
            log.warn("[分布式锁] 获取锁失败: {}", fullKey);
            // 抛出之前定义的业务异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后再试");
        }

        try {
            return joinPoint.proceed();
        } finally {
            // 4. 释放锁（仅释放自己持有的锁）
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[分布式锁] 已释放锁: {}", fullKey);
            }
        }
    }

    private String parseKey(String keySpel, ProceedingJoinPoint joinPoint) {
        if (!keySpel.contains("#")) return keySpel;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        return parser.parseExpression(keySpel).getValue(context, String.class);
    }
}