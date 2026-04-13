package me.link.bootstrap.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高性能系统时钟工具类。
 *
 * <p>通过内存缓存减少 System.currentTimeMillis() 的系统级调用开销。
 * 针对 Google Java 规范及生产环境资源管理进行了极致优化。
 */
public final class SystemClockUtils {

    /** 更新频率（毫秒）。10ms 兼顾了精度与性能。 */
    private static final long UPDATE_PERIOD_MS = 10L;

    private static final AtomicLong NOW = new AtomicLong(System.currentTimeMillis());

    /**
     * 使用单线程池进行调度。
     * 采用自定义线程工厂以确保线程可识别且为守护模式。
     */
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(
                    runnable -> {
                        Thread thread = new Thread(runnable, "system-clock-utils-updater");
                        thread.setDaemon(true);
                        return thread;
                    });

    static {
        // 启动周期性任务：直接调用 set，剔除极低概率的异常捕获以保持代码清爽
        SCHEDULER.scheduleAtFixedRate(
                () -> NOW.set(System.currentTimeMillis()),
                UPDATE_PERIOD_MS,
                UPDATE_PERIOD_MS,
                TimeUnit.MILLISECONDS);

        // 注册 JVM 关闭钩子，确保优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(SystemClockUtils::destroy));
    }

    /** 防止实例化。 */
    private SystemClockUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 获取当前缓存的时间戳。
     *
     * @return 当前毫秒数
     */
    public static long now() {
        return NOW.get();
    }

        /**
     * 获取当前缓存时间对应的 LocalDateTime。
     *
     * @return 当前 LocalDateTime
     */
    public static LocalDateTime localDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(now()), ZoneId.systemDefault());
    }

    /**
     * 显式停止时钟服务。
     * shutdown() 是幂等的，多次调用无副作用。
     */
    public static void destroy() {
        SCHEDULER.shutdown();
    }
}