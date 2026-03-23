package me.link.bootstrap.system.tenant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import me.link.bootstrap.system.tenant.vo.TenantPageReqVO;
import me.link.bootstrap.system.tenant.entity.SystemTenant;

/**
 * 租户服务接口
 */
public interface ISystemTenantService extends IService<SystemTenant> {

    /**
     * 创建租户并处理层级路径
     * @param tenant 租户信息
     * @return 租户ID
     */
    Long createTenant(SystemTenant tenant);

    /**
     * 校验租户是否可用
     * @param id 租户ID
     */
    void checkTenantValid(Long id);

    /**
     * 分页查询租户列表
     * @param pageReqVO 查询条件
     * @return 分页结果
     */
    IPage<SystemTenant> getTenantPage(TenantPageReqVO pageReqVO);
}