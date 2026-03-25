package me.link.bootstrap.framework.utils;

import cn.hutool.core.util.IdUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * 链路追踪工具类
 * 核心职责：管理基于 MDC 的 TraceId，支持手动设置、自动生成和跨线程传递
 */
public class TraceUtils {

    /**
     * 定义链路追踪 ID 在 MDC 中的键名，须与 logback-spring.xml 中的 %X{traceId} 保持一致
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 生成并设置新的随机 TraceId
     * 常用于：拦截器开始处、定时任务开始处、或异步线程确实拿不到主线程 ID 时
     */
    public static String setupTraceId() {
        String traceId = IdUtil.fastSimpleUUID(); // 使用更快的简易 UUID (不带横杠)
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    /**
     * 【新增】手动设置 TraceId
     * 常用于：异步线程从主线程接收 ID 后的注入动作
     *
     * @param traceId 外部传入的追踪 ID
     */
    public static void setTraceId(String traceId) {
        if (StringUtils.isNotBlank(traceId)) {
            MDC.put(TRACE_ID, traceId);
        }
    }

    /**
     * 获取当前线程的 TraceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 清除当前线程的 TraceId
     * 关键：在拦截器 afterCompletion 或异步线程 finally 块中调用，防止线程污染
     */
    public static void clear() {
        MDC.remove(TRACE_ID);
        // 如果你的业务中还有其他 MDC 变量，也可以选择 MDC.clear()
    }
}