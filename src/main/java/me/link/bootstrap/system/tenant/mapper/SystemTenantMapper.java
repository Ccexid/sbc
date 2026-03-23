package me.link.bootstrap.system.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.link.bootstrap.system.tenant.entity.SystemTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemTenantMapper extends BaseMapper<SystemTenant> {
}
