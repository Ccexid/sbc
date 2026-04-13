package me.link.bootstrap.modules.system.infrastructure.persistence.mapper;

import me.link.bootstrap.shared.infrastructure.mybatis.mapper.BaseMapperX;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.MenuPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuMapper extends BaseMapperX<MenuPO> {
}
