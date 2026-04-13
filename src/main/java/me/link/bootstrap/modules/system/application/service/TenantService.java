package me.link.bootstrap.modules.system.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import me.link.bootstrap.shared.kernel.pojo.SortablePageParam;
import me.link.bootstrap.modules.system.application.dto.vo.TenantExpiryRespVO;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantDO;

public interface TenantService extends IService<TenantDO> {
    /**
     * 判断租户是否已过期
     *
     * @param id 租户ID
     * @return {@link  TenantExpiryRespVO}
     */
    TenantExpiryRespVO isExpired(Long id);


    /**
     * 分页搜索租户信息
     *
     * @param param 可排序的分页参数，包含分页信息和排序字段配置
     *              <ul>
     *                  <li>sort: 排序字段字符串，多个字段使用逗号分隔，字段前加 - 表示降序</li>
     *                  <li>示例：-createTime,id 表示先按创建时间降序，再按 ID 升序</li>
     *              </ul>
     * @return {@link IPage}<{@link TenantDO}> 包含租户数据的分页结果，每页包含租户的详细信息
     */
    IPage<TenantDO> searchByPage(SortablePageParam  param);
}
