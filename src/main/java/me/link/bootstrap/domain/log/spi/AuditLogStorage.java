package me.link.bootstrap.domain.log.spi;

import me.link.bootstrap.domain.log.model.AuditLogDTO;

/**
 * 审计日志存储 SPI (领域层)
 * 定义了审计日志持久化的核心契约
 */
public interface AuditLogStorage {

    /**
     * 持久化审计日志
     *
     * @param dto 审计日志数据传输对象
     */
    void record(AuditLogDTO dto);

    /**
     * 存储器名称（用于在配置中指定使用哪个存储，或作为日志标识）
     * 示例：返回 "DB", "ES", "Redis", "Console"
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 是否启用该存储器
     * 作用：可以通过配置文件动态开关某个存储实现（如开发环境开控制台，生产环境开 ES）
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * 执行优先级
     * 数值越小优先级越高，用于决定多存储时的执行顺序
     */
    default int getOrder() {
        return 0;
    }
}