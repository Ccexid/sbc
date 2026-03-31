package me.link.bootstrap.util;

import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.exception.GlobalException;
import me.link.bootstrap.system.dal.mapper.SequenceMapper;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 分布式 ID 生成工具类，继承自 Hutool 的 IdUtil。
 * <p>
 * 该类利用 Redis (Redisson) 实现全局唯一且有序的序列号生成，支持按天重置序列或全局累加模式。
 * 生成的 ID 格式为：前缀 + 日期 (可选) + 指定位数的自增序列。
 * </p>
 * <p>
 * 核心特性：
 * 1. 高并发支持：基于 Redis 原子操作 guarantee 线程安全。
 * 2. 灵活策略：支持每日重置（含日期）或全局递增（不含日期）。
 * 3. 自动过期：按天模式下，计数器会在次日自动过期，节省内存。
 * 4. 降级容错：当 Redis 不可用时，自动降级至数据库序列表，最终保底使用时间戳。
 * </p>
 *
 * @author link
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdUtils extends IdUtil {

    /**
     * Redisson 客户端实例，用于操作分布式原子长整型计数器。
     * 通过构造函数注入，确保在 Spring 容器管理下可用。
     */
    private final RedissonClient redissonClient;

    /**
     * SequenceMapper 实例，用于数据库操作。
     * 作为 Redis 故障时的降级存储方案。
     */
    private final SequenceMapper sequenceMapper;

    /**
     * 日期格式化器，用于生成包含秒级精度的日期字符串 (yyyyMMddHHmmss)。
     * 主要用于构建高精度的 Redis Key，防止同一秒内的并发冲突（虽然原子操作已保证，但有助于调试和区分）。
     */
    private static final DateTimeFormatter DATE_SEQ_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 日期格式化器，用于生成业务可见的日期字符串 (yyyyMMdd)。
     * 当开启按天重置模式时，此日期将嵌入到最终生成的 ID 中。
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Redis Key 的基础前缀。
     * 所有生成的计数器 Key 都将以此开头，便于管理和监控。
     */
    private static final String BASE_PREFIX = "id_gen:";

    /**
     * 静态实例引用。
     * 由于 Spring 管理的 Bean 是实例变量，而对外提供的工具方法通常是静态的，
     * 因此需要通过此静态变量桥接，以便在静态方法中调用实例方法。
     */
    private static IdUtils instance;

    /**
     * Spring 容器启动后的初始化钩子。
     * 在 Bean 实例化完成后，将当前实例赋值给静态变量，使静态方法可用。
     */
    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 获取下一个分布式唯一 ID 的静态入口方法。
     * <p>
     * 该方法供外部业务直接调用，内部委托给实例方法处理具体逻辑。
     * </p>
     *
     * @param prefix  ID 的前缀标识，用于区分不同业务类型（如 "ORDER", "USER"）。
     * @param digit   自增序列号的位数（不足部分左侧补零）。例如 5 表示生成 "00001"。
     * @param isDaily 是否按天重置序列号。
     *                - true: 每天从 1 开始计数，生成的 ID 包含日期字符串 (yyyyMMdd)。
     *                - false: 全局累加计数，生成的 ID 不包含日期字符串，序列号永不自减。
     * @return 生成的唯一 ID 字符串。
     * @throws IllegalStateException 如果 Spring 容器尚未完成初始化，instance 为 null 时抛出。
     */
    public static String getNextId(String prefix, int digit, boolean isDaily) {
        if (instance == null) {
            throw new IllegalStateException("IdUtils 尚未初始化，请确保 Spring 容器已启动。");
        }
        return instance.nextId(prefix, digit, isDaily);
    }

    /**
     * 内部记录类，用于封装生成分布式序列所需的上下文信息。
     * <p>
     * 使用 record 可以自动生成构造函数、getter 等方法，使代码更简洁。
     * </p>
     *
     * @param key          Redis 中的完整 Key，用于存储当前业务的计数器。
     * @param bizName      业务标识名，用于数据库查询时的唯一键。
     * @param shouldExpire 标记该计数器是否需要设置过期时间（仅当 isDaily 为 true 时需要）。
     */
    private record IdContext(String key, String bizName, boolean shouldExpire) {
    }

    /**
     * 核心方法：生成下一个分布式唯一 ID。
     * <p>
     * 逻辑流程：
     * 1. 获取当前时间，格式化出用于构建 Key 的高精度日期串和用于结果展示的日期串。
     * 2. 根据 isDaily 参数决定业务名称（bizName）和 Redis Key 的结构。
     * 3. 构建上下文对象 (IdContext)。
     * 4. 调用 getSequence 获取自增序列号（优先 Redis，失败则降级 DB）。
     * 5. 将前缀、日期（可选）和格式化后的序列号拼接成最终结果返回。
     * </p>
     *
     * @param prefix  ID 前缀。
     * @param digit   序列号位数。
     * @param isDaily 是否按天重置。
     * @return 格式化后的唯一 ID 字符串。
     */
    public String nextId(String prefix, int digit, boolean isDaily) {
        // 获取当前日期字符串
        // dateStrSeq: 用于构建唯一的 Redis Key，精确到秒，减少极端并发下的哈希冲突概率（虽非必须，但有益）
        String dateStrSeq = LocalDateTime.now().format(DATE_SEQ_FORMATTER);
        // dateStr: 用于最终 ID 展示，精确到天
        String dateStr = LocalDateTime.now().format(DATE_FORMATTER);

        // 业务标识名，用于数据库查询
        // 按天模式：业务名包含日期，确保数据库中不同天的记录隔离（如果需要持久化每天的最大值）
        // 全局模式：业务名仅为前缀
        String bizName = isDaily ? prefix + ":" + dateStr : prefix;

        // 1. 构建上下文：决定 Redis Key 的结构以及是否需要过期策略
        var ctx = isDaily
                // 按天模式：Key 包含前缀和精确时间，设置过期标志为 true
                ? new IdContext("%s%s:%s".formatted(BASE_PREFIX, prefix, dateStr), bizName, true)
                // 全局模式：Key 包含前缀和 "global" 后缀，不过期
                : new IdContext("%s%s:global".formatted(BASE_PREFIX, prefix), bizName, false);

        // 2. 获取自增序列号（包含降级逻辑）
        long sequence = getSequence(ctx);

        // 3. 格式化输出：构建指定位数的数字字符串（例如 %05d）
        // 如果 digit <= 0，则不补零，直接输出数字
        String suffix = (digit > 0) ? "%0" + digit + "d" : "%d";
        
        // 拼接最终结果
        return isDaily
                ? "%s%s%s".formatted(prefix, dateStrSeq, suffix.formatted(sequence))
                : "%s%s".formatted(prefix, suffix.formatted(sequence));
    }

    /**
     * 从 Redis 获取并递增序列号，同时处理按天模式的过期设置。
     * <p>
     * 此方法是高性能路径，优先使用 Redis 原子操作。
     * </p>
     *
     * @param ctx 上下文对象，包含 Redis Key 和过期标志。
     * @return 递增后的序列号。
     */
    private long getSequence(IdContext ctx) {
        try {
            // 获取 Redis 原子长整型对象
            RAtomicLong atomicLong = redissonClient.getAtomicLong(ctx.key());
            
            // 执行原子自增操作，返回自增后的值
            long sequence = atomicLong.incrementAndGet();

            // 4. 使用 Instant 进行精确过期设置
            // 仅在序列号为 1（即当天的第一个请求/新创建的 Key）且需要过期时设置过期时间
            // 这样可以避免每次请求都去计算和设置过期时间，提升性能
            if (sequence == 1 && ctx.shouldExpire()) {
                // 计算当天 23:59:59.999... 的 Instant 时间点
                var endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                
                // 设置过期时间，确保每天的数据自动清理，防止内存无限增长
                // expire(Instant) 方法会让 Key 在指定时间点失效
                atomicLong.expire(endOfDay);
            }
            return sequence;
        } catch (GlobalException e) {
            // 捕获自定义全局异常（通常包装了 Redis 连接超时等异常）
            log.error("Redisson 异常，尝试从数据库维护表获取序列：{}", e.getMessage());
            // 降级：调用数据库序列生成方法
            return getSequence(ctx.bizName());
        } catch (Exception e) {
            // 捕获其他未预期的异常，同样触发降级
            log.error("Redis 操作发生未知异常，降级至数据库序列：{}", e.getMessage(), e);
            return getSequence(ctx.bizName());
        }
    }

    /**
     * 降级方法：当 Redis 不可用时，从数据库获取序列号。
     * <p>
     * 逻辑：
     * 1. 生成一个雪花算法 ID 作为基准（或直接使用当前时间戳）。
     * 2. 调用 Mapper 更新数据库中的最大值（upsert 并 increment）。
     * 3. 查询当前的最新值返回。
     * 4. 如果数据库也失败，则返回基于时间戳的简单保底值。
     * </p>
     * <b>注意</b>：此方法的并发性能和唯一性保证弱于 Redis 方案，仅作为故障转移手段。
     *
     * @param bizName 业务标识名，对应数据库中的业务键。
     * @return 序列号。
     */
    private long getSequence(String bizName) {
        try {
            // 生成一个初始序列号，这里使用雪花算法作为基础，避免纯时间戳的重复问题
            long sequence = IdUtils.getSnowflakeNextId();
            
            // 先进行原子操作进行数据更新获取最大序列号
            // upsertAndIncrement 应该保证在数据库层面的原子性（如使用 ON DUPLICATE KEY UPDATE 或 锁）
            sequenceMapper.upsertAndIncrement(sequence, bizName);
            
            // 返回更新后的当前值
            return sequenceMapper.selectCurrentValue(bizName);
        } catch (GlobalException ex) {
            // 数据库操作也失败时的最后保底策略
            log.error("数据库序列生成失败，使用时间戳最后保底！", ex);
            // 返回当前毫秒数的后 6 位，极度简化，仅保证短时内不完全重复，生产环境需谨慎
            return System.currentTimeMillis() % 1000000;
        } catch (Exception ex) {
            // 捕获其他数据库异常
            log.error("数据库操作发生未知异常，使用时间戳最后保底！", ex);
            return System.currentTimeMillis() % 1000000;
        }
    }
}
