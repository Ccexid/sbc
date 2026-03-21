package me.link.bootstrap.core.idempotent.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.exception.BusinessException;
import me.link.bootstrap.core.exception.ErrorCode;
import me.link.bootstrap.core.idempotent.annotation.Idempotent;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.utils.CacheUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "idempotent:token:";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        // 1. JDK 17 模式匹配：判断是否为控制器方法请求
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        // 2. 获取注解，若无则放行
        Idempotent idempotent = hm.getMethodAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        // 3. 获取 Header 中的 Token
        String token = request.getHeader(idempotent.headerName());
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求异常：幂等 Token 缺失");
        }

        // 4. 原子性删除校验
        String tenantId = TenantContextHolder.getTenantId();
        String redisKey = TOKEN_PREFIX + tenantId + ":" + token;

        try {
            boolean isRemoved = CacheUtils.delete(redisKey);
            if (!isRemoved) {
                // 删除失败说明 Token 不存在（已过期或已被使用）
                throw new BusinessException(ErrorCode.REPEATED_REQUESTS, idempotent.message());
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            // 兜底处理：Redis 连接异常等
            log.error("[Idempotent] 幂等校验服务异常 Key: {}", redisKey, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统繁忙，请稍后重试");
        }

        return true;
    }
}