package me.link.bootstrap.shared.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.shared.infrastructure.idgen.IdSegment;
import me.link.bootstrap.modules.system.infrastructure.persistence.mapper.SequenceMapper;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static me.link.bootstrap.shared.kernel.constant.GlobalConstants.FORMAT_YEAR_MONTH_DAY_COMPACT;
import static me.link.bootstrap.shared.kernel.constant.GlobalConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_COMPACT;

/**
 * 分布式ID生成器 - 工业级号段模式
 * 特性：高并发、零IO阻塞、Redis+DB双容错、内存泄露防护
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdUtil {

    private static final long DEFAULT_STEP = 1000;
    private static final String BASE_PREFIX = "id_gen:segment:";
    private static final DateTimeFormatter DATE_SEQ_FORMATTER = DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_COMPACT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_COMPACT);

    private final RedissonClient redissonClient;
    private final SequenceMapper sequenceMapper;

    private static IdUtil instance;
    private final Map<String, IdSegment> segmentCache = new ConcurrentHashMap<>();

    // 专门用于异步加载号段的线程池
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ExecutorService loaderExecutor = new ThreadPoolExecutor(
            2, 4, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            r -> {
                Thread thread = new Thread(r, "id-loader-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 初始化方法 - Spring容器启动时自动调用
     * 将当前实例赋值给静态变量，使静态方法能够访问Spring管理的Bean
     */
    @PostConstruct
    public void init() {
        instance = this;
        log.info("[IdUtils] 分布式ID生成器初始化完成 | RedissonClient={}, SequenceMapper={}", 
                redissonClient != null ? "已注入" : "未注入", 
                sequenceMapper != null ? "已注入" : "未注入");
    }

    /**
     * 销毁方法 - Spring容器关闭时自动调用
     * 优雅关闭异步加载线程池，防止资源泄露
     */
    @PreDestroy
    public void shutdown() {
        log.info("[IdUtils] 开始销毁分布式ID生成器 | 缓存号段数量={}", segmentCache.size());
        loaderExecutor.shutdown();
        try {
            if (!loaderExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                loaderExecutor.shutdownNow();
                log.warn("[IdUtils] 强制关闭异步加载线程池");
            }
        } catch (InterruptedException e) {
            loaderExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[IdUtils] 分布式ID生成器销毁完成");
    }

    /**
     * 获取分布式唯一ID（静态入口方法）
     * <p>
     * 支持两种ID格式：
     * 1. 按天重置：{prefix}{yyyyMMddHHmmss}{sequence} 例如：ORD20260409153045000001
     * 2. 全局递增：{prefix}{sequence} 例如：ORD000001
     * </p>
     *
     * @param prefix  业务前缀，用于区分不同业务场景（如：ORD-订单、USER-用户）
     * @param digit   序列号位数，不足左侧补0（建议6-8位）
     * @param isDaily 是否按天重置计数
     *                - true: 每天从0开始计数，ID包含日期信息
     *                - false: 全局连续递增，ID不包含日期
     * @return 生成的分布式唯一ID
     * @throws IllegalStateException 如果IdUtils未正确初始化
     */
    public static String next(String prefix, int digit, boolean isDaily) {
        if (instance == null) {
            log.error("[IdUtils] IdUtils未初始化，无法生成ID | prefix={}, digit={}, isDaily={}", prefix, digit, isDaily);
            throw new IllegalStateException("IdUtils未初始化");
        }
        return instance.generateId(prefix, digit, isDaily);
    }

    /**
     * 核心ID生成逻辑（实例方法）
     * <p>
     * 生成流程：
     * 1. 构建业务标识（bizName），区分按天和不按天的场景
     * 2. 获取或更新内存中的可用号段
     * 3. 异步预加载下一号段（无阻塞优化）
     * 4. 原子自增获取序列值
     * 5. 容错检查：如果号段耗尽则递归重试
     * 6. 清理过期的内存号段（防止内存泄露）
     * 7. 格式化组装最终ID
     * </p>
     *
     * @param prefix  业务前缀
     * @param digit   序列号位数
     * @param isDaily 是否按天重置
     * @return 生成的ID字符串
     */
    private String generateId(String prefix, int digit, boolean isDaily) {
        LocalDateTime now = SystemClockUtil.localDateTime();
        String todayStr = now.format(DATE_FORMATTER);
        // 构建业务标识：按天场景附加日期后缀，实现每日独立计数
        String bizName = isDaily ? prefix + ":" + todayStr : prefix;

        log.debug("[IdUtils] 开始生成ID | prefix={}, bizName={}, digit={}, isDaily={}", 
                prefix, bizName, digit, isDaily);

        // 步骤1：获取可用号段（可能触发同步加载）
        IdSegment segment = obtainSegment(bizName);
        log.debug("[IdUtils] 获取号段成功 | bizName={}, currentValue={}, maxId={}, remaining={}", 
                bizName, segment.getCurrentValue().get(), segment.getMaxId(),
                segment.getMaxId() - segment.getCurrentValue().get());

        // 步骤2：异步检测并预加载下一号段（核心优化：避免号段耗尽时的阻塞）
        preheatNextSegment(segment);

        // 步骤3：原子自增获取序列值（无锁高性能）
        long seq = segment.getAndIncrement();

        // 步骤4：容错检查 - 极端并发情况下可能击穿号段，执行递归重试
        if (seq > segment.getMaxId()) {
            log.warn("[IdUtils] 号段被击穿，执行降级重试 | bizName={}, seq={}, maxId={}", 
                    bizName, seq, segment.getMaxId());
            return generateId(prefix, digit, isDaily);
        }

        // 步骤5：定时清理过期内存Key（防止内存泄露）
        evictExpiredSegments(todayStr);

        // 步骤6：格式化组装ID
        String suffix = String.format("%0" + digit + "d", seq);
        String generatedId = isDaily ? prefix + now.format(DATE_SEQ_FORMATTER) + suffix : prefix + suffix;
        
        log.debug("[IdUtils] ID生成成功 | generatedId={}, seq={}, bizName={}", generatedId, seq, bizName);
        return generatedId;
    }

    /**
     * 获取或更新内存中的号段
     * <p>
     * 检查缓存中是否存在可用号段，如果不存在或已耗尽则触发同步加载
     * </p>
     *
     * @param bizName 业务标识
     * @return 可用的号段对象
     */
    private IdSegment obtainSegment(String bizName) {
        IdSegment segment = segmentCache.get(bizName);
        if (segment == null || segment.isExhausted()) {
            log.debug("[IdUtils] 号段不存在或已耗尽，触发同步加载 | bizName={}, exhausted={}", 
                    bizName, segment != null && segment.isExhausted());
            return provision(bizName);
        }
        return segment;
    }

    /**
     * 同步加载新号段（双重检查锁定模式）
     * <p>
     * 加载流程：
     * 1. 获取分布式锁，确保多节点间号段分配的原子性
     * 2. 二次检查缓存，避免重复加载
     * 3. 从Redis原子递增获取新号段范围
     * 4. 异步同步到数据库（最终一致性）
     * 5. 更新内存缓存
     * </p>
     * <p>
     * 容灾策略：
     * - Redis异常时降级为DB直接分配（性能降低但保证可用性）
     * - 分布式锁超时时间3秒，持锁时间10秒
     * </p>
     *
     * @param bizName 业务标识
     * @return 新加载的号段
     */
    private IdSegment provision(String bizName) {
        String lockKey = BASE_PREFIX + bizName + ":lock";
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取分布式锁：等待3秒，持锁10秒
            boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("[IdUtils] 获取分布式锁超时，执行DB降级 | bizName={}, lockKey={}", bizName, lockKey);
                return fallback(bizName);
            }

            log.debug("[IdUtils] 获取分布式锁成功，开始加载号段 | bizName={}", bizName);

            // 双重检查：获取锁后再次验证缓存，避免重复加载
            IdSegment current = segmentCache.get(bizName);
            if (current != null && !current.isExhausted()) {
                log.debug("[IdUtils] 双重检查发现号段已加载，直接返回 | bizName={}", bizName);
                return current;
            }

            // 从Redis提取新号段：原子递增获取号段结束值
            String redisKey = BASE_PREFIX + bizName;
            RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
            
            // 如果Redis中不存在该key，从数据库读取当前最大值进行初始化
            if (!atomicLong.isExists()) {
                Long dbMax = sequenceMapper.selectCurrentValueByName(bizName);
                long initValue = dbMax == null ? 0 : dbMax;
                atomicLong.set(initValue);
                log.info("[IdUtils] Redis号段初始化 | bizName={}, dbMax={}, initValue={}", 
                        bizName, dbMax, initValue);
            }

            // 原子递增：一次性获取整个号段的结束值
            long endId = atomicLong.addAndGet(DEFAULT_STEP);
            long startId = endId - DEFAULT_STEP + 1;
            
            log.info("[IdUtils] 从Redis分配新号段 | bizName={}, startId={}, endId={}, step={}", 
                    bizName, startId, endId, DEFAULT_STEP);

            // 异步同步到数据库，确保最终一致性（不阻塞主流程）
            loaderExecutor.execute(() -> {
                try {
                    int affectedRows = sequenceMapper.upsertValueIfGreater(bizName, endId);
                    log.debug("[IdUtils] 号段异步同步到DB成功 | bizName={}, endId={}, affectedRows={}", 
                            bizName, endId, affectedRows);
                } catch (Exception e) {
                    log.error("[IdUtils] 号段异步同步到DB失败 | bizName={}, endId={}", bizName, endId, e);
                }
            });

            // 创建新号段并放入缓存
            IdSegment newSegment = new IdSegment(startId, DEFAULT_STEP, bizName);
            segmentCache.put(bizName, newSegment);
            log.debug("[IdUtils] 号段加载完成并缓存 | bizName={}, cacheSize={}", 
                    bizName, segmentCache.size());
            return newSegment;
            
        } catch (InterruptedException e) {
            log.error("[IdUtils] 获取分布式锁被中断 | bizName={}", bizName, e);
            Thread.currentThread().interrupt();
            return fallback(bizName);
        } catch (Exception e) {
            log.error("[IdUtils] 号段加载失败，执行DB降级 | bizName={}, error={}", 
                    bizName, e.getMessage(), e);
        } finally {
            // 释放分布式锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[IdUtils] 释放分布式锁 | bizName={}", bizName);
            }
        }
        
        // 降级方案：直接从数据库分配
        return fallback(bizName);
    }

    /**
     * 检查并异步加载下一号段（无阻塞预加载机制）
     * <p>
     * 当号段使用达到阈值（剩余量 < 20%）时，提前异步加载下一号段，
     * 避免号段耗尽时的同步等待，实现零IO阻塞的高性能ID生成。
     * </p>
     * <p>
     * 使用本地锁防止单机内重复提交异步任务
     * </p>
     *
     * @param segment 当前使用的号段
     */
    private void preheatNextSegment(IdSegment segment) {
        // 检查是否达到预加载阈值（剩余量 < 20%）且没有正在进行的加载任务
        if (segment.isThresholdReached() && !segment.isLoading()) {
            log.debug("[IdUtils] 号段达到预加载阈值，触发异步加载 | bizName={}, currentValue={}, maxId={}, threshold=20%", 
                    segment.getBizName(), segment.getCurrentValue().get(), segment.getMaxId());
            
            // 本地锁防止单机内重复提交异步任务
            synchronized (segment) {
                if (!segment.isLoading()) {
                    segment.setLoading(true);
                    log.debug("[IdUtils] 提交异步加载任务 | bizName={}", segment.getBizName());
                    
                    loaderExecutor.execute(() -> {
                        try {
                            log.debug("[IdUtils] 开始异步加载下一号段 | bizName={}", segment.getBizName());
                            provision(segment.getBizName());
                            log.debug("[IdUtils] 异步加载下一号段完成 | bizName={}", segment.getBizName());
                        } catch (Exception e) {
                            log.error("[IdUtils] 异步加载下一号段失败 | bizName={}", segment.getBizName(), e);
                        } finally {
                            segment.setLoading(false);
                            log.debug("[IdUtils] 重置加载标志 | bizName={}", segment.getBizName());
                        }
                    });
                }
            }
        }
    }

    /**
     * 数据库降级方案
     * <p>
     * 当Redis不可用或分布式锁获取失败时，直接通过数据库分配ID。
     * 性能较低（每次都需要DB IO），但保证了系统的可用性。
     * </p>
     * <p>
     * 注意：此方法每次只分配1个ID，适用于低频场景或紧急降级
     * </p>
     *
     * @param bizName 业务标识
     * @return 单ID号段（step=1）
     */
    private IdSegment fallback(String bizName) {
        log.warn("[IdUtils] 执行DB降级分配ID | bizName={}", bizName);
        try {
            // 数据库原子递增
            int affectedRows = sequenceMapper.upsertAndIncrement(bizName);
            long val = sequenceMapper.selectCurrentValueByName(bizName);
            
            log.warn("[IdUtils] DB降级分配成功 | bizName={}, currentValue={}, affectedRows={} | 警告：性能较低，建议检查Redis状态", 
                    bizName, val, affectedRows);
            
            // 返回单ID号段（step=1）
            return new IdSegment(val, 1, bizName);
        } catch (Exception e) {
            log.error("[IdUtils] DB降级分配失败 | bizName={}", bizName, e);
            throw new RuntimeException("ID生成失败，请检查数据库连接", e);
        }
    }

    /**
     * 清理过期的内存号段（防止内存泄露）
     * <p>
     * 对于按天重置的场景（bizName包含日期），需要定期清理历史日期的号段数据。
     * 当缓存大小超过50个条目时触发清理，保留当天的号段，删除历史日期的号段。
     * </p>
     *
     * @param todayStr 今天的日期字符串（yyyyMMdd格式）
     */
    private void evictExpiredSegments(String todayStr) {
        // 当缓存过大时触发清理（阈值：50个条目）
        if (segmentCache.size() > 50) {
            int beforeSize = segmentCache.size();
            
            // 移除所有包含":"且不以后缀为今天日期的key（即历史日期的号段）
            segmentCache.keySet().removeIf(key -> key.contains(":") && !key.endsWith(todayStr));
            
            int afterSize = segmentCache.size();
            int removedCount = beforeSize - afterSize;
            
            if (removedCount > 0) {
                log.info("[IdUtils] 清理过期内存号段 | todayStr={}, beforeSize={}, afterSize={}, removedCount={}", 
                        todayStr, beforeSize, afterSize, removedCount);
            }
        }
    }
}