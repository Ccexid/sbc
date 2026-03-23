package me.link.bootstrap.system.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.link.bootstrap.core.exception.ErrorCode;
import me.link.bootstrap.core.exception.util.BusinessExceptionUtil;
import me.link.bootstrap.system.tenant.entity.SystemTenant;
import me.link.bootstrap.system.tenant.mapper.SystemTenantMapper;
import me.link.bootstrap.system.tenant.service.ISystemTenantService;
import me.link.bootstrap.system.tenant.vo.TenantPageReqVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 租户服务实现类
 */
@Service
public class SystemTenantServiceImpl extends ServiceImpl<SystemTenantMapper, SystemTenant> implements ISystemTenantService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTenant(SystemTenant tenant) {
        // 1. 处理上级路径逻辑
        if (tenant.getParentId() == null || tenant.getParentId() == 0L) {
            tenant.setParentId(0L);
            tenant.setTenantPath("0");
        } else {
            SystemTenant parent = getById(tenant.getParentId());
            if (parent == null) {
                throw BusinessExceptionUtil.exception(ErrorCode.BAD_REQUEST, "上级租户不存在");
            }
            tenant.setTenantPath(parent.getTenantPath() + "," + parent.getId());
        }

        baseMapper.insert(tenant);
        return tenant.getId();
    }

    @Override
    public void checkTenantValid(Long id) {
        SystemTenant tenant = getById(id);
        if (tenant == null) {
            throw BusinessExceptionUtil.exception(ErrorCode.TENANT_NOT_FOUND);
        }
        if (tenant.getStatus() != 0) {
            throw BusinessExceptionUtil.exception(ErrorCode.TENANT_FORBIDDEN);
        }
        if (tenant.getExpireTime() != null && LocalDateTime.now().isAfter(tenant.getExpireTime())) {
            throw BusinessExceptionUtil.exception(ErrorCode.TENANT_EXPIRED);
        }
    }

    @Override
    public IPage<SystemTenant> getTenantPage(TenantPageReqVO pageReqVO) {
        // 1. 构建分页对象
        Page<SystemTenant> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<SystemTenant> wrapper = new LambdaQueryWrapper<SystemTenant>()
                .like(StringUtils.hasText(pageReqVO.getTenantName()), SystemTenant::getTenantName, pageReqVO.getTenantName())
                .like(StringUtils.hasText(pageReqVO.getContactUser()), SystemTenant::getContactUser, pageReqVO.getContactUser())
                .eq(StringUtils.hasText(pageReqVO.getTenantType()), SystemTenant::getTenantType, pageReqVO.getTenantType())
                .eq(pageReqVO.getStatus() != null, SystemTenant::getStatus, pageReqVO.getStatus())
                .orderByDesc(SystemTenant::getId); // 默认 ID 倒序

        return baseMapper.selectPage(page, wrapper);
    }
}