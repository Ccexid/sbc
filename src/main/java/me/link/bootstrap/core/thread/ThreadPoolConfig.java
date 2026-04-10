package me.link.bootstrap.core.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 核心业务异步线程池
     * 优化点：
     * 1. 禁用核心线程超时，保障突发流量下的响应速度。
     * 2. 维持 CallerRunsPolicy，但需通过监控手段观察主线程阻塞情况。
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 规模配置（建议根据 Nacos/Apollo 动态配置，此处为静态默认值）
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);

        // 2. 核心优化：关闭核心线程超时
        // 除非业务极度稀疏，否则常驻核心线程可显著降低任务启动延迟
        executor.setAllowCoreThreadTimeOut(false);

        // 3. 标识配置
        executor.setThreadNamePrefix("link-async-");

        // 4. 上下文传递：集成 TraceId 跨线程装饰器
        executor.setTaskDecorator(new TraceTaskDecorator());

        // 5. 拒绝策略：使用调用者执行策略
        // 注意：若任务执行极慢，该策略会导致提交任务的 Web 线程阻塞，起到天然背压作用，
        // 但需警惕因异步任务过重导致全站响应变慢（雪崩风险）。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 6. 优雅停机：确保 JVM 关闭前尝试处理完积压任务
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}