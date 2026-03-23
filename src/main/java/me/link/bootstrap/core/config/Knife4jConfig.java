package me.link.bootstrap.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Link-Boot 数字化平台接口文档")
                        .version("1.0.0")
                        .description("基于 P2S2B2C 架构的多租户系统"));
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1-系统模块")
                .packagesToScan("me.link.bootstrap.system.controller")
                // 仅匹配 /system 开头的接口
                .pathsToMatch("/system/**")
                .build();
    }

    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("2-业务模块")
                .packagesToScan("me.link.bootstrap.business.controller")
                .pathsToMatch("/v1/**")
                .build();
    }

}