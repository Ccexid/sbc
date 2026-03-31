package me.link.bootstrap.system.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.link.bootstrap.system.dal.domain.SequenceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Update("INSERT INTO system_sequence (id, name, current_value, version, create_time, update_time) " +
            "VALUES (#{id}, #{name}, 1, 0, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE current_value = current_value + 1, update_time = NOW()")
    int upsertAndIncrement(@Param("id") Long id, @Param("name") String name);

    /**
     * 获取当前的序列值
     * * @param name 业务标识
     *
     * @return 当前最大序列号
     */
    @Select("SELECT current_value FROM system_sequence WHERE name = #{name}")
    Long selectCurrentValue(@Param("name") String name);
}
