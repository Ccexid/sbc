package me.link.bootstrap.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.link.bootstrap.system.dal.domain.TenantPackageDO;
import me.link.bootstrap.system.dal.mapper.TenantPackageMapper;
import me.link.bootstrap.system.service.TenantPackageService;
import org.springframework.stereotype.Service;

@Service
public class TenantPackageServiceImpl extends ServiceImpl<TenantPackageMapper, TenantPackageDO> implements TenantPackageService {
}
