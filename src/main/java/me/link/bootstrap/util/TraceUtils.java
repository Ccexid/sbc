package me.link.bootstrap.util;

import cn.hutool.core.util.IdUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.slf4j.MDC;

import static me.link.bootstrap.core.constants.GlobalApiConstants.*;

/**
 * 链路追踪工具类（UUID 高性能版）
 * <p>
 * 采用 Hutool 的 fastSimpleUUID 生成 TraceId，结合 TransmittableThreadLocal 确保跨线程传递。
 * 相比于序列号方案，去除了锁竞争和时间格式化，具有更高的并发吞吐量。
 * </p>
 */
public class TraceUtils {

    /**
     * 基于 TTL 的上下文持有者，确保 TraceId 在线程池中能够正确传递
     */
    private static final TransmittableThreadLocal<String> TRACE_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 初始化并生成新的 TraceId
     * <p>
     * 使用 Hutool 的 fastSimpleUUID 生成 32 位无连字符的唯一标识，
     * 格式为：{项目名}_{UUID}
     * </p>
     *
     * @return 新生成的 TraceId
     */
    public static String create() {
        // 使用 fastSimpleUUID 性能更优（基于性能更高的随机算法，且不带横线）
        String traceId = TRACE_ID_PREFIX + "_" + IdUtil.fastSimpleUUID();
        set(traceId);
        return traceId;
    }

    /**
     * 获取当前线程上下文中的 TraceId
     * <p>
     * 优先从 TTL 中获取，以支持跨线程透传场景。
     * </p>
     *
     * @return 当前线程的 TraceId，不存在时返回 null
     */
    public static String get() {
        String traceId = TRACE_HOLDER.get();
        if (traceId != null) {
            return traceId;
        }
        // 降级从 MDC 获取，增加健壮性
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 将 TraceId 设置到当前线程上下文
     *
     * @param traceId 要设置的 TraceId
     */
    public static void set(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            TRACE_HOLDER.set(traceId);
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    /**
     * 清理当前线程上下文
     * <p>
     * 在 Filter 的 finally 块中调用，确保线程复用时的安全性。
     * </p>
     */
    public static void remove() {
        TRACE_HOLDER.remove();
        MDC.remove(TRACE_ID_KEY);
    }
}