package me.link.bootstrap.core.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.util.TraceUtils;

import java.io.IOException;
import java.util.regex.Pattern;

import static me.link.bootstrap.core.constants.GlobalApiConstants.TRACE_ID_HEADER;

/**
 * 链路追踪过滤器（性能与安全加固版）
 * <p>
 * 将链路入口从 Interceptor 提升至 Filter，确保更早的初始化与更彻底的清理
 */
@Slf4j
public class TraceFilter implements Filter {

    // 规则：^[a-zA-Z0-9]+_[a-f0-9]{32}$
    private static final Pattern TRACE_PATTERN = Pattern.compile("^[a-zA-Z0-9]+_[a-f0-9]{32}$");

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            String traceId = request.getHeader(TRACE_ID_HEADER);

            // 1. 合法性检查与上下文初始化
            if (isValidTraceId(traceId)) {
                TraceUtils.set(traceId);
            } else {
                traceId = TraceUtils.create();
            }

            // 2. 响应回显：在执行后续链条前设置，确保在 Response Committed 之前生效
            if (!response.isCommitted()) {
                response.setHeader(TRACE_ID_HEADER, traceId);
            }

            // 3. 继续执行过滤链
            chain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            // 5. 记录其他运行时异常，并包装为 ServletException 向上抛出
            log.error("TraceFilter encountered an unexpected exception", e);
            throw new ServletException(e);
        } finally {
            // 5. 核心：彻底清理上下文，防止线程池污染及内存泄漏
            TraceUtils.remove();
        }
    }

    private boolean isValidTraceId(String traceId) {
        return traceId != null && TRACE_PATTERN.matcher(traceId).matches();
    }
}