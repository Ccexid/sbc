package me.link.bootstrap.modules.system.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.modules.system.domain.model.entity.Tenant;
import me.link.bootstrap.modules.system.domain.repository.TenantRepository;
import me.link.bootstrap.modules.system.infrastructure.converter.TenantConverter;
import me.link.bootstrap.modules.system.infrastructure.persistence.mapper.TenantMapper;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 租户仓储实现
 * <p>
 * 职责：
 * - 实现领域层定义的 TenantRepository 接口
 * - 使用 Converter 进行 PO ↔ Entity 转换
 * - 封装 MyBatis-Plus 的数据访问细节
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantMapper tenantMapper;
    private final TenantConverter tenantConverter;

    @Override
    public Tenant findById(Long id) {
        TenantPO po = tenantMapper.selectById(id);
        return po != null ? tenantConverter.toEntity(po) : null;
    }

    @Override
    public void save(Tenant tenant) {
        TenantPO po = tenantConverter.toPO(tenant);
        tenantMapper.insert(po);
        // 回填生成的 ID
        tenant.setId(po.getId());
    }

    @Override
    public void update(Tenant tenant) {
        TenantPO po = tenantConverter.toPO(tenant);
        tenantMapper.updateById(po);
    }

    @Override
    public void delete(Long id) {
        tenantMapper.deleteById(id);
    }

    @Override
    public List<Tenant> findByCondition(String keyword, Integer status) {
        LambdaQueryWrapper<TenantPO> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(TenantPO::getName, keyword);
        }
        
        if (status != null) {
            wrapper.eq(TenantPO::getStatus, status);
        }
        
        List<TenantPO> poList = tenantMapper.selectList(wrapper);
        return tenantConverter.toEntityList(poList);
    }

    @Override
    public Page<Tenant> findByPage(int pageNo, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<TenantPO> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(TenantPO::getName, keyword);
        }
        
        if (status != null) {
            wrapper.eq(TenantPO::getStatus, status);
        }
        
        Page<TenantPO> poPage = tenantMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        
        // 转换为领域实体分页
        Page<Tenant> entityPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        entityPage.setRecords(tenantConverter.toEntityList(poPage.getRecords()));
        
        return entityPage;
    }
}
