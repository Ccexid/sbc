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
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 深度优化的多租户拦截器
 * 适配 P2S2B2C 架构，支持 Sa-Token 会话绑定与 Header 穿透
 */
@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_ID_HEADER = "X-Tenant-Id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 1. 如果请求的不是控制器方法（例如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String tenantIdStr = null;

        // 2. 优先级解析策略：会话存储 > HTTP Header
        // 登录后的 Session 是服务器可信数据，优先级最高，防止前端 Header 篡改越权
        if (StpUtil.isLogin()) {
            Object sessionTenantId = StpUtil.getSession().get("tenantId");
            if (sessionTenantId != null) {
                tenantIdStr = sessionTenantId.toString();
            }
        }

        // 3. 若 Session 无值，尝试从 Header 获取（用于登录前或特定 OpenApi 场景）
        if (!StringUtils.hasText(tenantIdStr)) {
            tenantIdStr = request.getHeader(TENANT_ID_HEADER);
        }

        // 4. 核心校验逻辑
        if (StringUtils.hasText(tenantIdStr)) {
            try {
                // 转换为 Long，确保与数据库层 BaseTenantEntity 类型一致
                long tenantId = Long.parseLong(tenantIdStr);

                // 基础业务校验：租户 ID 必须大于 0
                if (tenantId <= 0) {
                    throw BusinessExceptionUtil.exception(ErrorCode.BAD_REQUEST, "无效的租户 ID");
                }

                TenantContextHolder.setTenantId(tenantIdStr);
                return true;
            } catch (NumberFormatException e) {
                log.error("[租户拦截器] 路径: {}, 租户 ID 格式错误: {}", request.getRequestURI(), tenantIdStr);
                throw BusinessExceptionUtil.exception(ErrorCode.PARAM_ERROR, "租户 ID 格式非法");
            }
        }

        // 5. 阻断处理：根据登录状态返回更精准的错误码
        log.warn("[租户拦截器] 拦截到非法请求: {}, 租户信息缺失", request.getRequestURI());

        if (!StpUtil.isLogin()) {
            // 未登录，提示去登录
            throw BusinessExceptionUtil.exception(ErrorCode.UNAUTHORIZED);
        } else {
            // 已登录但拿不到租户 ID，说明账号异常或数据配置错误
            throw BusinessExceptionUtil.exception(ErrorCode.TENANT_NOT_FOUND);
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 核心：请求结束务必清理，防止 Tomcat 线程池环境下发生租户数据串改
        TenantContextHolder.clear();
        log.trace("[租户拦截器] 已清理上下文: {}", request.getRequestURI());
    }
}