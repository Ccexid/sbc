package me.link.bootstrap.shared.infrastructure.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置类
 * <p>
 * 定义系统中使用的各类线程池Bean，包括核心业务异步线程池和日志记录线程池。
 * 所有线程池均集成TraceId跨线程传递功能，并配置了优雅关闭策略。
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    /**
     * 创建核心业务异步线程池。
     * <p>
     * 该线程池用于处理系统中的核心异步业务逻辑，具有以下特性：
     * <ul>
     *   <li>常驻核心线程，降低任务启动延迟</li>
     *   <li>集成TraceId装饰器，支持链路追踪上下文传递</li>
     *   <li>使用CallerRunsPolicy拒绝策略，提供天然背压机制</li>
     *   <li>支持优雅关闭，确保JVM退出前完成积压任务</li>
     * </ul>
     *
     * @return 配置好的ThreadPoolTaskExecutor实例，Bean名称为"asyncExecutor"
     */
    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 配置线程池规模参数：核心线程数、最大线程数、队列容量及空闲线程存活时间
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);

        // 禁用核心线程超时，保持核心线程常驻以应对突发流量
        executor.setAllowCoreThreadTimeOut(false);

        // 设置线程名称前缀，便于日志排查和问题定位
        executor.setThreadNamePrefix("link-async-");

        // 集成TraceId跨线程传递装饰器，确保链路追踪上下文不丢失
        executor.setTaskDecorator(new TraceTaskDecorator());

        // 设置拒绝策略：由调用线程执行任务，防止任务丢失并提供背压保护
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 配置优雅关闭：等待已有任务完成后再关闭线程池，最多等待60秒
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }

    /**
     * 创建日志记录专用异步线程池。
     * <p>
     * 该线程池专门用于处理异步日志记录任务，具有较大的队列容量以缓冲突发日志流量。
     * 采用CallerRunsPolicy拒绝策略防止日志丢失，同时集成TraceId装饰器支持链路追踪。
     *
     * @return 配置好的ThreadPoolTaskExecutor实例，Bean名称为"logExecutor"
     */
    @Bean
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 配置线程池规模：较小的核心线程数配合较大的队列容量，适合日志这种IO密集型任务
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        
        // 设置拒绝策略：由调用线程处理任务，防止日志丢失并提供背压机制
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 设置优雅关闭：等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // 集成TraceId跨线程传递装饰器，确保日志中包含完整的链路追踪信息
        executor.setTaskDecorator(new TraceTaskDecorator());
        
        // 设置线程名称前缀，便于区分日志线程和业务线程
        executor.setThreadNamePrefix("link-log-");
        
        executor.initialize();
        return executor;
    }
}