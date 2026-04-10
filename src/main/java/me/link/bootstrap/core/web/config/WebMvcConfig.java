package me.link.bootstrap.core.web.config;

import me.link.bootstrap.core.constants.GlobalApiConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(GlobalApiConstants.API_PREFIX + "/**") // 仅对 API 前缀生效
                .allowedOriginPatterns("*") // 生产环境建议指定具体域名
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(GlobalApiConstants.TRACE_ID_HEADER)
                .allowCredentials(true)
                .maxAge(3600);
    }
}
