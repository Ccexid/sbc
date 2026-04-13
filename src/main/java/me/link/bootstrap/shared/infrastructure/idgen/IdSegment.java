package me.link.bootstrap.shared.infrastructure.idgen;

import lombok.Data;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 号段实体：支撑双号段无阻塞生成的内存模型
 * <p>
 * 用于分布式ID生成器中管理一段连续的ID序列，支持基于阈值的预加载机制
 * 通过双号段切换实现无阻塞的ID生成，保证高并发场景下的性能
 * </p>
 */
@Data
public class IdSegment {
    /**
     * 当前ID值，使用原子类保证线程安全
     */
    private final AtomicLong currentValue;

    /**
     * 号段最大ID值
     */
    private final long maxId;

    /**
     * 业务名称，用于区分不同业务的号段
     */
    private final String bizName;

    /**
     * 号段步长，即每次从数据库获取的ID数量
     */
    private final long step;

    /**
     * 加载状态标识，防止多线程重复触发号段加载
     * 使用 volatile 保证多线程可见性
     */
    private volatile boolean loading = false;

    /**
     * 构造号段实例
     *
     * @param startId 起始ID值
     * @param step 号段步长，即每次从数据库获取的ID数量
     * @param bizName 业务名称，用于区分不同业务的号段
     */
    public IdSegment(long startId, long step, String bizName) {
        this.currentValue = new AtomicLong(startId);
        this.maxId = startId + step - 1;
        this.step = step;
        this.bizName = bizName;
    }

    /**
     * 获取并递增当前ID值
     * <p>
     * 原子操作，保证多线程环境下的ID唯一性
     * </p>
     *
     * @return 当前ID值，递增前的值
     */
    public long getAndIncrement() {
        return currentValue.getAndIncrement();
    }

    /**
     * 判定号段是否达到预加载阈值
     * <p>
     * 当剩余可用ID数量小于号段步长的20%时触发预加载，确保在当前号段耗尽前完成新号段的获取
     * </p>
     *
     * @return true-已达到预加载阈值，需要开始加载新号段；false-未达到阈值
     */
    public boolean isThresholdReached() {
        return (maxId - currentValue.get()) < (step * 0.2);
    }

    /**
     * 判定号段是否已彻底耗尽
     * <p>
     * 当当前ID值超过最大ID值时，表示该号段已完全使用完毕
     * </p>
     *
     * @return true-号段已耗尽；false-号段仍有可用ID
     */
    public boolean isExhausted() {
        return currentValue.get() > maxId;
    }
}
