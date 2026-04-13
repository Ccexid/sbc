package me.link.bootstrap.shared.infrastructure.converter;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct 全局配置
 * <p>
 * 定义统一的转换策略：
 * - 未映射字段发出警告而非错误
 * - 使用 Spring 组件模型，支持依赖注入
 * - 统一命名规范
 * </p>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.WARN,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface BaseConverter {
}
