package me.link.bootstrap.util;

import cn.hutool.core.date.DateUtil;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicInteger;

import static me.link.bootstrap.core.constants.GlobalApiConstants.*;

/**
 * 链路追踪工具类（优化版）
 * <p>
 * 提供 TraceId 的生成、获取、设置和清理功能，基于 MDC 实现线程级别的链路追踪上下文管理
 * 通过时间字符串缓存和无锁序列号生成优化性能
 * </p>
 */
public class TraceUtils {

    /**
     * 序列号生成器，用于保证同一时间戳内 TraceId 的唯一性
     */
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    /**
     * 缓存上一分钟的时间字符串，减少 format 调用次数
     */
    private static volatile String lastMinuteStr = "";

    /**
     * 上次更新时间戳，用于判断是否需要刷新时间字符串缓存
     */
    private static volatile long lastUpdateMillis = 0L;

    /**
     * 初始化并生成新的 TraceId
     * <p>
     * 根据当前时间（分钟级精度）和自增序列号生成唯一标识，格式为：{项目名}-{时间戳}-{序列号}
     * 生成的 TraceId 会自动设置到 MDC 上下文中，供后续日志记录使用
     * </p>
     * <p>
     * 注意：调用方应在使用完毕后调用 {@link #remove()} 方法清理 MDC 上下文，避免内存泄漏
     * </p>
     *
     * @return 新生成的 TraceId
     */
    public static String create() {
        String timeStr = formatTimestamp();

        int currentSeq = SEQ.getAndIncrement();
        if (currentSeq > MAX_SEQ_LENGTH) {
            SEQ.set(1);
            currentSeq = 1;
        }

        String traceId = APP_PROJECT_NAME + "-" + timeStr + "-" + String.format("%06d", currentSeq);

        MDC.put(TRACE_ID_HEADER, traceId);
        return traceId;
    }

    /**
     * 获取分钟级时间字符串（带简单的缓存策略）
     * <p>
     * 通过缓存机制减少日期格式化操作，在同一分钟内复用已格式化的时间字符串
     * 使用双重检查锁定确保线程安全
     * </p>
     *
     * @return 格式化后的时间字符串，格式为 yyyyMMddHHmm
     */
    private static String formatTimestamp() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateMillis < 60000L && !lastMinuteStr.isEmpty()) {
            return lastMinuteStr;
        }
        synchronized (TraceUtils.class) {
            if (now - lastUpdateMillis >= 60000L) {
                lastMinuteStr = DateUtil.format(DateUtil.date(now), FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                lastUpdateMillis = now;
            }
        }
        return lastMinuteStr;
    }

    /**
     * 获取当前线程上下文中的 TraceId
     * <p>
     * 从 MDC 中获取之前设置的 TraceId，如果未设置则返回 null
     * </p>
     *
     * @return 当前线程的 TraceId，不存在时返回 null
     */
    public static String get() {
        return MDC.get(TRACE_ID_HEADER);
    }

    /**
     * 手动设置 TraceId 到当前线程上下文
     * <p>
     * 适用于跨线程传递或从外部系统接收 TraceId 的场景
     * </p>
     *
     * @param traceId 要设置的 TraceId，如果为 null 则不执行任何操作
     */
    public static void set(String traceId) {
        if (traceId != null) {
            MDC.put(TRACE_ID_HEADER, traceId);
        }
    }

    /**
     * 清理当前线程上下文中的 TraceId
     * <p>
     * 必须在请求处理完毕或线程任务结束后调用，防止 MDC 上下文污染和内存泄漏
     * 特别在线程池场景中，不清理会导致后续复用该线程的请求继承错误的 TraceId
     * </p>
     */
    public static void remove() {
        MDC.remove(TRACE_ID_HEADER);
    }
}