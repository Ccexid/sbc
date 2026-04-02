package me.link.bootstrap.core.web.config;

import cn.hutool.http.HtmlUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigInteger;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomizer() {
        return builder -> {
            // 1. 基础配置：显式注册 JSR310 日期模块
            builder.modules(new JavaTimeModule());
            builder.featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

            // 2. 解决 Long 精度丢失问题 (输出给前端时转 String)
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.serializerByType(BigInteger.class, ToStringSerializer.instance);

            // 3. XSS 全局防注入 (接收前端 JSON 字符串时进行过滤)
            builder.deserializerByType(String.class, new JsonDeserializer<String>() {
                @Override
                public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getValueAsString();
                    if (value == null) {
                        return null;
                    }
                    // 使用 HtmlUtil.filter 剔除脚本标签，保留安全 HTML
                    return HtmlUtil.filter(value);
                }
            });
        };
    }
}