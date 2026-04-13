package me.link.bootstrap.shared.utils;

import com.alibaba.ttl.threadpool.TtlExecutors;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 线程池增强工具类
 * 提供对原生线程池的 TTL 增强包装，支持跨线程链路追踪
 */
public class ThreadPoolUtils {

    private ThreadPoolUtils() {
        // 工具类私有构造，防止实例化
    }

    /**
     * 包装原生 Executor，使其具备 TTL 跨线程传递能力
     * <p>
     * TTL 内部已实现幂等性检查，重复包装会返回原实例
     * </p>
     *
     * @param executor 待包装的 Executor
     * @return 包装后的 Executor
     * @throws IllegalArgumentException 如果 executor 为 null
     */
    public static Executor wrap(Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("Executor must not be null");
        }
        return TtlExecutors.getTtlExecutor(executor);
    }

    /**
     * 包装原生 ExecutorService，使其具备 TTL 跨线程传递能力
     * <p>
     * TTL 内部已实现幂等性检查，重复包装会返回原实例
     * </p>
     *
     * @param service 待包装的 ExecutorService
     * @return 包装后的 ExecutorService
     * @throws IllegalArgumentException 如果 service 为 null
     */
    public static ExecutorService wrapService(ExecutorService service) {
        if (service == null) {
            throw new IllegalArgumentException("ExecutorService must not be null");
        }
        return TtlExecutors.getTtlExecutorService(service);
    }
}