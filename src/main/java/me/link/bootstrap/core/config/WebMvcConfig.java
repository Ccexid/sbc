package me.link.bootstrap.core.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.idempotent.interceptor.IdempotentInterceptor;
import me.link.bootstrap.core.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j // 核心：启用 Lombok 日志
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
        // ========== 2. 先打印配置日志，确认白名单是否加载 ==========
        log.info("========== 拦截器配置开始 ==========");
        log.info("系统默认排除路径: {}", DEFAULT_EXCLUDE_PATHS);

        List<String> customIgnoreUrls = whiteListProperties.getIgnoreUrls();
        log.info("自定义白名单路径: {}", customIgnoreUrls);

        // ========== 4. 租户拦截器（添加配置日志） ==========
        log.info("注册租户拦截器: 拦截路径=/**, 排除路径={}",
                combinePaths(customIgnoreUrls));
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(DEFAULT_EXCLUDE_PATHS)
                .excludePathPatterns(customIgnoreUrls)
                .order(1);

        // ========== 5. 幂等拦截器（添加配置日志） ==========
        log.info("注册幂等拦截器: 拦截路径=/**, 排除路径={}",
                combinePaths(customIgnoreUrls));
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(DEFAULT_EXCLUDE_PATHS)
                .excludePathPatterns(customIgnoreUrls)
                .order(2);

        log.info("========== 拦截器配置完成 ==========");
    }

    // 辅助方法：合并两个路径列表，方便打印
    private List<String> combinePaths(List<String> urls) {
        return List.of(String.join(", ", WebMvcConfig.DEFAULT_EXCLUDE_PATHS), String.join(", ", urls));
    }
}