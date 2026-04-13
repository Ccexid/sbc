package me.link.bootstrap.modules.system.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantPackagePO;
import me.link.bootstrap.modules.system.infrastructure.persistence.mapper.TenantPackageMapper;
import me.link.bootstrap.modules.system.application.service.TenantPackageService;
import org.springframework.stereotype.Service;

@Service
public class TenantPackageServiceImpl extends ServiceImpl<TenantPackageMapper, TenantPackagePO> implements TenantPackageService {
}
