package me.link.bootstrap.core.config;

import me.link.bootstrap.core.tenant.TenantContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Locale;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "auditLogExecutor")
    public Executor auditLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-log-");
        // 挂载上下文装饰器，实现“传送门”功能
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * 上下文拷贝装饰器：确保异步线程能拿到主线程的租户和语言信息
     */
    static class ContextCopyingDecorator implements TaskDecorator {
        @Override
        @NonNull
        public Runnable decorate(@NonNull Runnable runnable) {
            // [主线程] 获取信息
            String tenantId = TenantContextHolder.getTenantId();
            Locale locale = LocaleContextHolder.getLocale();

            return () -> {
                try {
                    // [异步线程] 注入信息
                    TenantContextHolder.setTenantId(tenantId);
                    LocaleContextHolder.setLocale(locale);
                    runnable.run();
                } finally {
                    // [异步线程] 任务结束必须清理，防止线程复用污染
                    TenantContextHolder.clear();
                    LocaleContextHolder.resetLocaleContext();
                }
            };
        }
    }
}