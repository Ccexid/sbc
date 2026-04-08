package me.link.bootstrap.system.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.link.bootstrap.core.mybatis.mapper.BaseMapperX;
import me.link.bootstrap.system.dal.domain.OrganizationDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrganizationMapper extends BaseMapperX<OrganizationDO> {
}
