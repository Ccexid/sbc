package me.link.bootstrap.core.config;

import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.idempotent.interceptor.IdempotentInterceptor;
import me.link.bootstrap.core.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final IdempotentInterceptor idempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 租户拦截器优先级最高
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 2. 幂等拦截器紧随其后
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/swagger-ui/**", "/v3/api-docs/**")
                .order(2);
    }
}