package me.link.bootstrap.shared.infrastructure.web.config;

import me.link.bootstrap.shared.kernel.constant.GlobalConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(GlobalConstants.API_PREFIX + "/**") // 仅对 API 前缀生效
                .allowedOriginPatterns("*") // 生产环境建议指定具体域名
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(GlobalConstants.TRACE_ID_HEADER)
                .allowCredentials(true)
                .maxAge(3600);
    }
}
