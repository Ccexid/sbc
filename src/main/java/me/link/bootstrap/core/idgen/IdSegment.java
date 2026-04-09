package me.link.bootstrap.core.idgen;

import lombok.Data;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段实体：支撑双号段无阻塞生成的内存模型
 */
@Data
public class IdSegment {
    private final AtomicLong currentValue;
    private final long maxId;
    private final String bizName;
    private final long step;
    // 使用 volatile 保证多线程可见性
    private volatile boolean loading = false;

    public IdSegment(long startId, long step, String bizName) {
        this.currentValue = new AtomicLong(startId);
        this.maxId = startId + step - 1;
        this.step = step;
        this.bizName = bizName;
    }

    public long getAndIncrement() {
        return currentValue.getAndIncrement();
    }

    /**
     * 判定号段是否达到预加载阈值：剩余量 < 20%
     */
    public boolean isThresholdReached() {
        return (maxId - currentValue.get()) < (step * 0.2);
    }

    /**
     * 判定号段是否已彻底耗尽
     */
    public boolean isExhausted() {
        return currentValue.get() > maxId;
    }
}
