package me.link.bootstrap.modules.system.application.assembler;

import me.link.bootstrap.modules.system.application.dto.command.CreateTenantCmd;
import me.link.bootstrap.modules.system.application.dto.vo.TenantVO;
import me.link.bootstrap.modules.system.domain.model.entity.Tenant;
import me.link.bootstrap.shared.infrastructure.converter.BaseConverter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 租户应用层对象转换器
 * <p>
 * 职责：
 * - Command → Entity（创建场景）
 * - Entity → VO（查询场景）
 * </p>
 * <p>
 * 与 Infrastructure 层的 Converter 区别：
 * - Assembler 只处理应用层 DTO 与领域实体的转换
 * - Converter 处理基础设施层 PO 与领域实体的转换
 * </p>
 */
@Mapper(config = BaseConverter.class)
public interface TenantAssembler extends BaseConverter {

    TenantAssembler INSTANCE = Mappers.getMapper(TenantAssembler.class);

    // ==================== Command → Entity ====================

    /**
     * 创建命令转领域实体
     * <p>
     * 将创建租户的命令对象转换为领域实体，忽略由系统自动生成的字段
     * </p>
     *
     * @param cmd 创建租户命令对象
     * @return 租户领域实体
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
     * <p>
     * 将租户领域实体转换为视图对象，用于前端展示
     * </p>
     *
     * @param tenant 租户领域实体
     * @return 租户视图对象
     */
    TenantVO toVO(Tenant tenant);

    /**
     * 领域实体列表转视图对象列表
     * <p>
     * 批量将租户领域实体列表转换为视图对象列表，用于列表展示
     * </p>
     *
     * @param tenantList 租户领域实体列表
     * @return 租户视图对象列表
     */
    List<TenantVO> toVOList(List<Tenant> tenantList);
}
