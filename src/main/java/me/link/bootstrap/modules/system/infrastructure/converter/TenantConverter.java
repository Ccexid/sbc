package me.link.bootstrap.modules.system.infrastructure.converter;

import me.link.bootstrap.modules.system.application.dto.command.CreateTenantCmd;
import me.link.bootstrap.modules.system.application.dto.vo.TenantVO;
import me.link.bootstrap.modules.system.domain.model.entity.Tenant;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantPO;
import me.link.bootstrap.shared.infrastructure.converter.BaseConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 租户对象转换器
 * <p>
 * 负责以下转换：
 * - PO（持久化对象） ↔ Entity（领域实体）
 * - Command（命令对象） → Entity（领域实体）
 * - Entity（领域实体） → VO（视图对象）
 * </p>
 */
@Mapper(config = BaseConverter.class)
public interface TenantConverter extends BaseConverter {

    /**
     * 获取单例实例（用于非 Spring 环境）
     */
    TenantConverter INSTANCE = Mappers.getMapper(TenantConverter.class);

    // ==================== PO ↔ Entity ====================

    /**
     * PO 转领域实体
     *
     * @param po 持久化对象
     * @return 领域实体
     */
    Tenant toEntity(TenantPO po);

    /**
     * 领域实体转 PO
     *
     * @param tenant 领域实体
     * @return 持久化对象
     */
    TenantPO toPO(Tenant tenant);

    /**
     * PO 列表转领域实体列表
     *
     * @param poList PO 列表
     * @return 领域实体列表
     */
    List<Tenant> toEntityList(List<TenantPO> poList);

    // ==================== Command → Entity ====================

    /**
     * 创建命令转领域实体
     * <p>
     * 注意：ID、createTime 等字段由系统自动生成，不在 Command 中传递
     * </p>
     *
     * @param cmd 创建命令
     * @return 领域实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Tenant toEntity(CreateTenantCmd cmd);

    // ==================== Entity → VO ====================

    /**
     * 领域实体转视图对象
     *
     * @param tenant 领域实体
     * @return 视图对象
     */
    TenantVO toVO(Tenant tenant);

    /**
     * 领域实体列表转视图对象列表
     *
     * @param tenantList 领域实体列表
     * @return 视图对象列表
     */
    List<TenantVO> toVOList(List<Tenant> tenantList);
}
