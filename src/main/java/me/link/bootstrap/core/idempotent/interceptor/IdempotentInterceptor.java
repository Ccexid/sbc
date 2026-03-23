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
import me.link.bootstrap.core.utils.IdempotentSignUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 增强型幂等性拦截器 (JDK 17 优化版)
 * 职责：验签防篡改 -> 提取 UUID -> 原子删除 Redis Key
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "idempotent:token:";

    @Value("${app.idempotent.salt:LinkStart_2026}")
    private String salt;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        // 1. JDK 17 模式匹配：判断是否为控制器方法请求，非方法请求直接放行
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        // 2. 获取注解，若方法上未标记 @Idempotent 则放行
        Idempotent idempotent = hm.getMethodAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        // 3. 获取 Header 中的 Token (格式应为 uuid.signature)
        String publicToken = request.getHeader(idempotent.headerName());
        if (!StringUtils.hasText(publicToken)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请求异常：幂等 Token 缺失");
        }

        // 4. Token 格式校验与安全验签
        if (!publicToken.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法幂等令牌格式");
        }

        String[] parts = publicToken.split("\\.");
        String uuid = parts[0];
        String signature = parts[1];
        String tenantId = TenantContextHolder.getTenantId();

        // 5. 校验签名：利用工具类验证 (UUID + TenantId) 是否被篡改
        // 这一步在应用层完成，不产生 Redis IO，性能极高
        boolean isValid = IdempotentSignUtils.verify(uuid + tenantId, salt, signature);
        if (!isValid) {
            log.warn("[Idempotent] 发现非法签名尝试！Token: {}, TenantId: {}", publicToken, tenantId);
            throw new BusinessException(ErrorCode.PARAM_ERROR, "幂等令牌签名无效");
        }

        // 6. 原子性删除校验 (真正的幂等检查)
        // Key 仅使用 uuid 部分，配合租户隔离
        String redisKey = TOKEN_PREFIX + tenantId + ":" + uuid;

        try {
            // delete 操作是原子的，返回 true 表示该 Token 是第一次被使用且成功销毁
            boolean isRemoved = CacheUtils.delete(redisKey);
            if (!isRemoved) {
                // 删除失败说明 Token 不存在（已过期）或已被其他请求使用（重复提交）
                throw new BusinessException(ErrorCode.REPEATED_REQUESTS, idempotent.message());
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            // 兜底处理：如 Redis 服务宕机
            log.error("[Idempotent] 幂等校验服务异常 Key: {}", redisKey, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统校验异常，请稍后重试");
        }

        return true;
    }
}