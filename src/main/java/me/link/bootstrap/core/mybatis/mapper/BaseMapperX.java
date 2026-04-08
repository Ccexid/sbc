package me.link.bootstrap.core.mybatis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 扩展的MyBatis-Plus基础Mapper接口
 * <p>
 * 继承自MyBatis-Plus的BaseMapper，提供通用的CRUD操作能力。
 * 可通过泛型T指定实体类型，为具体的Mapper接口提供基础数据访问功能。
 * </p>
 *
 * @param <T> 实体类型
 * @see com.baomidou.mybatisplus.core.mapper.BaseMapper
 */
public interface BaseMapperX<T> extends BaseMapper<T> {
}
