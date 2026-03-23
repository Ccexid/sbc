package me.link.bootstrap.core.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.log.entity.AuditLogEntity;
import me.link.bootstrap.core.log.mapper.AuditLogMapper;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 高性能布隆过滤器预热任务 (支持多表并行、多租户穿透)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BloomFilterWarmUp implements CommandLineRunner {

    private final BloomFilterService bloomFilterService;
    private final AuditLogMapper auditLogMapper;

    // 每批次处理数据量，防止大数据量导致 OOM
    private static final int BATCH_SIZE = 5000;

    @Override
    public void run(String... args) {
        log.info(">>> [BloomFilter] 系统启动，开始执行异步预热...");

        // 1. 初始化过滤器：预期 500 万数据，1% 误判率
        RBloomFilter<String> filter = bloomFilterService.createOrGetFilter(5000000L, 0.01);

        // 2. 创建并行预热线程池 (建议生产环境使用自定义配置的 ThreadPoolTaskExecutor)
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            // 3. 异步并行加载多张表
            // 【核心修复】：在 CompletableFuture 的 Lambda 内部显式设置子线程的忽略标记
            CompletableFuture<Void> auditTask = CompletableFuture.runAsync(() -> {
                try {
                    // 必须在子线程起始位置设置，否则由于 ThreadLocal 隔离，拦截器无法获取该标记
                    TenantContextHolder.setIgnore(true);
                    log.debug(">>> [BloomFilter] 子线程 [{}] 租户忽略模式已开启", Thread.currentThread().getName());

                    loadTableWithBatch(filter, "audit", auditLogMapper, AuditLogEntity::getBusinessId);
                } finally {
                    // 任务结束务必清理，防止线程池复用污染后续任务
                    TenantContextHolder.clear();
                }
            }, executor);

            // 如果有其他表，可以继续添加 Task 并行执行
            // CompletableFuture<Void> userTask = CompletableFuture.runAsync(() -> { ... }, executor);

            // 等待所有异步任务完成
            CompletableFuture.allOf(auditTask).join();

            log.info(">>> [BloomFilter] 预热任务全量完成，当前过滤器元素计数: {}", filter.count());

        } catch (Exception e) {
            log.error(">>> [BloomFilter] 预热过程中发生严重异常", e);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * 通用的分批数据加载逻辑
     */
    private <T> void loadTableWithBatch(RBloomFilter<String> filter, String prefix,
                                        BaseMapper<T> mapper, Function<T, String> idExtractor) {
        log.info(">>> [BloomFilter] 正在加载业务表: {}, 线程: {}", prefix, Thread.currentThread().getName());

        long pageNum = 1;
        long totalItems = 0;

        while (true) {
            // 物理分页：BATCH_SIZE 条每页，不执行 Count 查询以优化性能
            Page<T> page = new Page<>(pageNum, BATCH_SIZE, false);

            // 仅查询业务 ID 字段，减少网络传输和内存开销
            QueryWrapper<T> wrapper = new QueryWrapper<T>().select("business_id");
            List<T> records = mapper.selectPage(page, wrapper).getRecords();

            if (records == null || records.isEmpty()) {
                break;
            }

            // 批量添加到布隆过滤器
            records.stream()
                    .map(idExtractor)
                    .filter(id -> id != null && !id.isBlank())
                    .map(id -> prefix + ":" + id) // 添加 Namespace 前缀防止多表 ID 碰撞
                    .forEach(filter::add);

            totalItems += records.size();
            log.debug(">>> [BloomFilter] [{}] 已同步 {} 条数据...", prefix, totalItems);
            pageNum++;
        }
        log.info(">>> [BloomFilter] [{}] 加载完毕，总计条数: {}", prefix, totalItems);
    }
}