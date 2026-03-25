package me.link.bootstrap.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.framework.interceptor.TraceInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 安全权限配置类
 * <p>
 * 主要职责：
 * 1. 实现 WebMvcConfigurer 接口，用于注册自定义拦截器。
 * 2. 配置全局路由拦截规则，实现基于 Sa-Token 的登录认证与权限校验。
 * 3. 定义无需登录即可访问的白名单路径（如登录接口、文档、监控等）。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SaTokenSecurityConfig implements WebMvcConfigurer {

    private final TraceInterceptor traceInterceptor;

    private static final String[] EXCLUDE_PATHS = {
            "/auth/login",
            "/h2-console/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/druid/**",
            "/favicon.ico",
            "/error" // Spring Boot 默认错误页面也建议放行
    };

    /**
     * 注册 Sa-Token 路由拦截器到 Spring MVC 拦截器链中
     * <p>
     * 此方法在 Spring 容器初始化时自动调用，用于将自定义的安全拦截逻辑注入到请求处理流程中。
     *
     * @param registry Spring 提供的拦截器注册表，用于添加自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("========== 拦截器配置开始 ==========");
        log.info("系统默认排除路径: {}", Arrays.stream(EXCLUDE_PATHS));
        // 注册链路追踪拦截器 (优先级最高)
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/**")
                .order(Ordered.HIGHEST_PRECEDENCE);
        // 注册一个 SaInterceptor 拦截器实例，并定义具体的拦截逻辑
        registry.addInterceptor(new SaInterceptor(handler -> {

                    // 【步骤 1】配置全局登录校验规则
                    // 作用：匹配所有请求路径 (/**)，但排除掉指定的白名单路径。
                    // 对于非白名单路径，执行 .check() 中的逻辑，即强制要求用户必须已登录。
                    SaRouter.match("/**")
                            .notMatch(EXCLUDE_PATHS)        // [白名单] 网站图标：浏览器自动请求的静态资源，无需鉴权)
                            // 执行校验：如果当前会话未登录，Sa-Token 将抛出异常并中断请求，返回未登录提示
                            .check(r -> StpUtil.checkLogin());

                    // 【步骤 2】角色或权限校验预留位置
                    // 作用：此处可用于扩展更细粒度的权限控制。
                    // 示例逻辑：当访问 /admin/** 开头的路径时，额外检查当前登录用户是否拥有 "admin" 角色。
                    // 使用方法：取消下方注释并根据实际业务需求修改路径和角色名称。
                    SaRouter.match("/admin/**", r -> StpUtil.checkRole("admin"));

                }))
                // 设置该拦截器生效的路径模式，这里设置为拦截所有进入应用的 HTTP 请求
                .addPathPatterns("/**").order(1);
        log.info("========== 拦截器配置完成 ==========");
    }
}