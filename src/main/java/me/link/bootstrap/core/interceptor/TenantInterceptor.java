package me.link.bootstrap.core.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.exception.ErrorCode;
import me.link.bootstrap.core.exception.util.BusinessExceptionUtil;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 1. 尝试多渠道获取租户 ID
        String tenantId = request.getHeader("X-Tenant-Id");

        if (StpUtil.isLogin()) {
            // 登录后的 Session 优先级最高（防篡改）
            Object sessionTenantId = StpUtil.getSession().get("tenantId");
            if (sessionTenantId != null) {
                tenantId = sessionTenantId.toString();
            }
        }

        // 2. 核心逻辑：强制校验
        // 只有获取到 tenantId 时才存入上下文并放行
        if (tenantId != null && !tenantId.isBlank()) {
            try {
                TenantContextHolder.setTenantId(tenantId);
                return true;
            } catch (NumberFormatException e) {
                log.error("[租户拦截器] 租户 ID 格式非法: {}", tenantId);
                throw BusinessExceptionUtil.exception(ErrorCode.BAD_REQUEST, "租户 ID 格式错误");
            }
        }

        // 3. 阻断非法请求
        // 如果运行到这里，说明既没登录也没传 Header
        log.warn("[租户拦截器] 请求路径 {} 缺少租户信息，请求已被阻断", request.getRequestURI());

        // 抛出业务异常，由全局异常处理器捕获返回给前端
        throw BusinessExceptionUtil.exception(ErrorCode.UNAUTHORIZED);
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 无论成功还是异常，必须清理 ThreadLocal 避免内存泄漏
        TenantContextHolder.clear();
    }
}