package me.link.bootstrap.framework.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.link.bootstrap.framework.utils.TraceUtils;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 链路追踪拦截器：负责 traceId 的生成、传递与清理
 */
@Component
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 步骤 1: 尝试从 HTTP 请求头中获取上游服务传递过来的 traceId
        // 作用：实现微服务间的链路追踪上下文传递，确保整个调用链使用同一个 traceId
        String traceId = request.getHeader(TraceUtils.TRACE_ID);

        if (traceId == null || traceId.isEmpty()) {
            // 步骤 2: 如果请求头中没有 traceId（通常是链路起点），则生成一个新的标准 UUID
            // 作用：为当前请求创建一个唯一的追踪标识，格式示例：413de0df-e6f3-4c25-845e-3bcd1e7981b1
            traceId = TraceUtils.setupTraceId();
        } else {
            // 步骤 3: 如果获取到了上游传来的 traceId，将其放入 MDC (Mapped Diagnostic Context)
            // 作用：绑定到当前线程，使得后续在该线程中执行的日志输出自动携带此 traceId
            MDC.put(TraceUtils.TRACE_ID, traceId);
        }

        // 步骤 4: 将 traceId 设置到 HTTP 响应头中
        // 作用：方便前端开发者在浏览器 Network 面板直接查看当前请求的追踪 ID，便于排查问题
        response.setHeader(TraceUtils.TRACE_ID, traceId);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 步骤 5: 请求处理完成后，清理当前线程的 MDC 数据
        // 作用：防止在使用线程池的环境下，线程被复用导致旧的 traceId 污染新的请求日志（内存泄漏或日志错乱）
        TraceUtils.clear();
    }
}