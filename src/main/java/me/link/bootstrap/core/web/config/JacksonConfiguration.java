package me.link.bootstrap.core.web.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;

@Configuration
public class JacksonConfiguration {

    /**
     * 使用 Customizer 而不是手动 new ObjectMapper
     * 这样可以确保 application.yml 中的 jackson 配置（如 SNAKE_CASE）依然生效
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomizer() {
        return builder -> {
            // 1. 显式注册 JSR310 日期模块
            builder.modules(new JavaTimeModule());
            builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
            // 2. 解决全栈开发中的 Long 精度丢失问题
            // 将 Long 和 BigInteger 全部序列化为 String 返回给前端
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.serializerByType(BigInteger.class, ToStringSerializer.instance);
        };
    }
}