package me.link.bootstrap.modules.system.infrastructure.persistence.mapper;

import me.link.bootstrap.shared.infrastructure.mybatis.mapper.BaseMapperX;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantMapper extends BaseMapperX<TenantPO> {
}
