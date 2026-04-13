package me.link.bootstrap.shared.infrastructure.web.config;

import jakarta.servlet.DispatcherType;
import me.link.bootstrap.shared.infrastructure.web.filter.TraceFilter;
import me.link.bootstrap.shared.infrastructure.web.filter.XssFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.EnumSet;

@Configuration
public class FilterConfig {

    /**
     * 链路追踪过滤器：优先级最高 (HIGHEST_PRECEDENCE)
     * 增加对 ASYNC 类型支持，确保异步 Web 请求（非线程池异步）也能正确传递上下文
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistration() {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceFilter");
        // 全量捕获不同类型的请求分发，确保链路完整性
        registration.setDispatcherTypes(EnumSet.of(
                DispatcherType.REQUEST,
                DispatcherType.FORWARD,
                DispatcherType.INCLUDE,
                DispatcherType.ASYNC
        ));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    /**
     * XSS 过滤器：优先级次之
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        // 建议：如果业务 API 有统一前缀，可在此缩小范围，减少静态资源的正则匹配开销
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        // 仅在 REQUEST 和 ASYNC 时处理 XSS，FORWARD/INCLUDE 通常内部受控，可根据需求精简
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}