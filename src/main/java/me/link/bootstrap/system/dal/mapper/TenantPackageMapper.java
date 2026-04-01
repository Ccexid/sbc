package me.link.bootstrap.system.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.link.bootstrap.system.dal.domain.TenantPackageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantPackageMapper extends BaseMapper<TenantPackageDO> {
}
