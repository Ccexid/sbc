package me.link.bootstrap.framework.config.decorator;

import me.link.bootstrap.framework.utils.TraceUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

public class ContextCopyingDecorator implements TaskDecorator {
    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // 1. [主线程] 提取
        String traceId = TraceUtils.getTraceId();

        return () -> {
            try {
                // 2. [子线程] 注入逻辑
                if (StringUtils.isNotBlank(traceId)) {
                    // 如果主线程有 ID，直接注入，实现链路串联
                    TraceUtils.setTraceId(traceId);
                } else {
                    // 如果主线程没有（极少发生），则为子线程生成独立 ID，保证日志不空
                    TraceUtils.setupTraceId();
                }

                runnable.run();
            } finally {
                // 3. [子线程] 清理
                TraceUtils.clear();
            }
        };
    }
}
