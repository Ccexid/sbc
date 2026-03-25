package me.link.bootstrap.framework.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import me.link.bootstrap.framework.config.decorator.ContextCopyingDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    /**
     * 创建支持 TraceId 传递的线程池执行器
     * <p>
     * 配置核心线程数 10、最大线程数 20、队列容量 200
     * 使用 TTL 包装确保 MDC 上下文（traceId）在线程间传递
     *
     * @return Executor 支持 TraceId 传递的线程池执行器
     */
    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("link-async-log");
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }
}
