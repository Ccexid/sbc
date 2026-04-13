package me.link.bootstrap.modules.system.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.SequenceDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SequenceMapper extends BaseMapper<SequenceDO> {
    /**
     * 原子递增序列值 (核心容灾逻辑)
     * 1. 如果 name 不存在，则插入一条记录，初始值为 1
     * 2. 如果 name 已存在，则在当前 currentValue 基础上加 1
     * * 使用 MySQL 的 ON DUPLICATE KEY UPDATE 保证并发下的原子性
     * * @param name 业务标识
     *
     * @return 影响行数
     */
    int upsertAndIncrement(@Param("name") String name);

    /**
     * 获取当前的序列值
     * * @param name 业务标识
     *
     * @return 当前最大序列号
     */
    Long selectCurrentValueByName(@Param("name") String name);


    /**
     * 同步 Redis 中的序列值到数据库
     * <p>
     * 将 Redis 中缓存的当前序列值同步到数据库，使用 UPSERT 模式：
     * 如果业务标识不存在则插入新记录；如果已存在则更新 current_value 为两者中的较大值
     * </p>
     *
     * @param name         业务标识
     * @param currentValue Redis 中缓存的当前序列值
     * @return 影响行数
     */
    int upsertValueIfGreater(@Param("name") String name, @Param("currentValue") Long currentValue);
}
