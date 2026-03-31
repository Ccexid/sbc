package me.link.bootstrap.system.dal.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统序列号实体类
 * 对应数据库表：system_sequence
 * 用于生成分布式唯一序列号（如订单号、流水号等）
 */
@Data
@TableName("system_sequence")
public class SequenceDO {

    /**
     * 主键 ID
     * 使用 ASSIGN_ID 策略，由 MyBatis Plus 内置的雪花算法自动生成全局唯一 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务标识名称
     * 用于区分不同业务场景的序列号，例如：
     * - "FD" 代表基金业务
     * - "PD:20260330" 代表特定日期的产品业务
     * 该字段在查询时需保持唯一性
     */
    private String name;

    /**
     * 当前最大序列号值
     * 每次获取新序列号时，该值会递增并作为返回结果
     * 初始值通常为 0 或 1，具体取决于业务需求
     */
    private Long currentValue;

    /**
     * 乐观锁版本号
     * 用于并发控制，防止多线程环境下序列号生成冲突
     * 每次更新操作时版本号自动 +1
     */
    @Version
    private Long version;

    /**
     * 记录创建时间
     * 插入数据时由框架自动填充当前时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 记录最后更新时间
     * 插入或更新数据时由框架自动填充当前时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
