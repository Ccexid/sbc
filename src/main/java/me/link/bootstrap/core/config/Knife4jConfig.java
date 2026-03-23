package me.link.bootstrap.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.parameters.Parameter;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Link-Boot 数字化平台")
                        .version("1.0.0")
                        .description("P2S2B2C 多租户架构管理后台"))
                .components(new Components()
                        // 在此处定义全局 Header 参数
                        .addParameters("TenantHeader", new Parameter()
                                .name("X-Tenant-Id")
                                .description("租户 ID")
                                .in("header")
                                .required(false)
                                .schema(new io.swagger.v3.oas.models.media.StringSchema()._default("1")))
                        .addParameters("AuthHeader", new Parameter()
                                .name("Authorization")
                                .description("登录 Token")
                                .in("header")
                                .required(false)));
    }
}