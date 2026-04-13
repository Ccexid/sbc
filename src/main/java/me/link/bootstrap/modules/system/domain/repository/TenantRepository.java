package me.link.bootstrap.modules.system.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.link.bootstrap.modules.system.domain.model.entity.Tenant;

import java.util.List;

/**
 * 租户仓储接口
 * 定义租户领域对象的数据访问操作
 */
public interface TenantRepository {
    /**
     * 根据ID查询租户
     *
     * @param id 租户ID
     * @return 租户实体，不存在时返回null
     */
    Tenant findById(Long id);

    /**
     * 保存新租户
     *
     * @param tenant 待保存的租户实体
     */
    void save(Tenant tenant);

    /**
     * 更新租户信息
     *
     * @param tenant 待更新的租户实体
     */
    void update(Tenant tenant);

    /**
     * 删除租户
     *
     * @param id 租户ID
     */
    void delete(Long id);

    /**
     * 根据条件查询租户列表
     *
     * @param keyword 搜索关键词
     * @param status 租户状态
     * @return 符合条件的租户列表
     */
    List<Tenant> findByCondition(String keyword, Integer status);

    /**
     * 分页查询租户
     *
     * @param pageNo 页码，从1开始
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @param status 租户状态
     * @return 分页结果
     */
    Page<Tenant> findByPage(int pageNo, int pageSize, String keyword, Integer status);
}
