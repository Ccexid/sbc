package me.link.bootstrap.shared.infrastructure.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import me.link.bootstrap.shared.infrastructure.web.servlet.XssHttpServletRequestWrapper;

import java.io.IOException;

/**
 * XSS 过滤器
 * <p>
 * 实现 Servlet Filter 接口，用于拦截和处理 HTTP 请求，防止跨站脚本攻击（XSS）。
 * 通过将请求包装在 XssHttpServletRequestWrapper 中，自动对所有输入进行 HTML 过滤处理。
 * </p>
 */
public class XssFilter implements Filter {
    /**
     * 执行过滤操作
     * <p>
     * 将原始请求包装为 XssHttpServletRequestWrapper，然后传递给过滤器链中的下一个过滤器或目标资源。
     * 包装后的请求会自动对头部和参数进行 XSS 过滤处理。
     *
     * @param servletRequest  原始 Servlet 请求对象，会被转换为 HttpServletRequest 并包装
     * @param servletResponse Servlet 响应对象
     * @param filterChain     过滤器链，用于继续执行后续的过滤器或目标资源
     * @throws IOException      当发生输入输出异常时抛出
     * @throws ServletException 当发生 Servlet 相关异常时抛出
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        /*
         * 创建 XSS 防护的请求包装器并继续执行过滤器链
         * 所有后续处理都将使用经过 XSS 过滤的请求对象
         */
        filterChain.doFilter(new XssHttpServletRequestWrapper((HttpServletRequest) servletRequest), servletResponse);
    }
}
