package me.link.bootstrap.core.config;

import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.idempotent.interceptor.IdempotentInterceptor;
import me.link.bootstrap.core.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final IdempotentInterceptor idempotentInterceptor;
    private final WhiteListProperties whiteListProperties;

    // 定义系统默认必须排除的路径（Swagger、错误页面、静态资源）
    private static final List<String> DEFAULT_EXCLUDE_PATHS = List.of(
            "/error",
            "/favicon.ico",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/doc.html"
    );

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 获取配置文件中的自定义白名单
        List<String> customIgnoreUrls = whiteListProperties.getIgnoreUrls();

        // 1. 租户拦截器
        // 建议：租户拦截器通常拦截所有业务路径，但必须排除系统级白名单和登录/注册等业务白名单
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(DEFAULT_EXCLUDE_PATHS)
                .excludePathPatterns(customIgnoreUrls)
                .order(1);

        // 2. 幂等拦截器
        // 幂等通常只针对 POST/PUT 请求，且排除掉所有白名单
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(DEFAULT_EXCLUDE_PATHS)
                .excludePathPatterns(customIgnoreUrls)
                .order(2);
    }
}