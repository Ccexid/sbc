package me.link.bootstrap.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.link.bootstrap.system.controller.vo.TenantExpiryRespVO;
import me.link.bootstrap.system.dal.domain.TenantDO;

public interface TenantService extends IService<TenantDO> {
    /**
     * 判断租户是否已过期
     *
     * @param id 租户ID
     * @return {@link  TenantExpiryRespVO}
     */
    TenantExpiryRespVO isExpired(Long id);
}
