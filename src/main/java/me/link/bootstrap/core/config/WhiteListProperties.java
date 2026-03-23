package me.link.bootstrap.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.white-list")
public class WhiteListProperties {
    /**
     * 忽略拦截的 URL 列表
     */
    private List<String> ignoreUrls = new ArrayList<>();
}