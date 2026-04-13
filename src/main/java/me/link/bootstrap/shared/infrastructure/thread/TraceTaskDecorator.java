package me.link.bootstrap.shared.infrastructure.thread;

import me.link.bootstrap.shared.util.TraceUtil;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 上下文装饰器：实现从主线程到子线程的“快照”传递
 */
public class TraceTaskDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // [主线程] 提取上下文快照
        String traceId = TraceUtil.get();
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        return () -> {
            // [子线程] 备份当前线程原有的 MDC 上下文，用于恢复
            Map<String, String> originalMdc = MDC.getCopyOfContextMap();
            try {
                // 1. 恢复主线程的 MDC 上下文
                if (mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                } else {
                    MDC.clear();
                }

                // 2. 确保 TraceId 在 TTL 和 MDC 中一致
                // 即使 mdcContext 中已有 TraceId，再次调用 set() 可确保 TRACE_HOLDER 也被正确设置
                if (traceId != null) {
                    TraceUtil.set(traceId);
                }

                runnable.run();
            } finally {
                // [子线程] 清理并恢复上下文
                // 1. 清理当前任务的 Trace 信息
                TraceUtil.remove();
                
                // 2. 恢复子线程原有的 MDC 上下文，避免污染线程池复用
                if (originalMdc != null) {
                    MDC.setContextMap(originalMdc);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
