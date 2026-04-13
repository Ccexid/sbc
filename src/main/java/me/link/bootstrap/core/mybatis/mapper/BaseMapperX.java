package me.link.bootstrap.core.mybatis.mapper;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import me.link.bootstrap.core.domain.BaseDO;
import me.link.bootstrap.core.pojo.FieldMaskParam;

/**
 * 扩展的MyBatis-Plus基础Mapper接口
 * <p>
 * 继承自MyBatis-Plus的BaseMapper，提供通用的CRUD操作能力。
 * 可通过泛型T指定实体类型，为具体的Mapper接口提供基础数据访问功能。
 * </p>
 *
 * @param <T> 实体类型
 * @see com.baomidou.mybatisplus.core.mapper.BaseMapper
 */
public interface BaseMapperX<T extends BaseDO> extends BaseMapper<T> {

    /**
     * 根据字段掩码进行选择性更新
     * <p>
     * 基于FieldMaskParam中指定的字段掩码，仅更新实体中掩码包含的字段到数据库。
     * 该方法通过实体的主键作为更新条件，避免全量更新导致的数据覆盖风险。
     * 支持前端传递下划线或驼峰命名的字段名，内部会自动转换匹配。
     * </p>
     *
     * @param entity    实体对象，包含要更新的字段值和主键ID
     * @param maskParam 字段掩码参数对象，指定需要更新的字段列表
     * @return 受影响的记录数，如果没有有效字段更新则返回0
     * @throws RuntimeException 当实体主键ID为null时抛出异常
     */
    default int updatePartially(T entity, FieldMaskParam maskParam) {
        if (entity == null || maskParam == null || maskParam.getMaskSet().isEmpty()) {
            return 0;
        }

        // 1. 获取表信息
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entity.getClass());
        if (tableInfo == null) return 0;

        // 2. 创建 UpdateWrapper
        UpdateWrapper<T> updateWrapper = new UpdateWrapper<>();

        // 3. 设置主键条件 (根据 ID 更新)
        Object id = ReflectUtil.getFieldValue(entity, tableInfo.getKeyProperty());
        if (id == null) {
            throw new RuntimeException("Entity ID (Primary Key) cannot be null for partial update.");
        }
        updateWrapper.eq(tableInfo.getKeyColumn(), id);

        // 4. 遍历掩码并填充 SET 字段
        boolean hasSet = false;
        for (String fieldName : maskParam.getMaskSet()) {
            // 统一处理：前端传下划线或驼峰都能兼容
            String propertyName = StrUtil.toCamelCase(fieldName);

            // 匹配数据库字段
            for (TableFieldInfo field : tableInfo.getFieldList()) {
                if (field.getProperty().equals(propertyName)) {
                    Object value = ReflectUtil.getFieldValue(entity, propertyName);
                    updateWrapper.set(field.getColumn(), value);
                    hasSet = true;
                    break;
                }
            }
        }

        // 5. 如果没有匹配到任何有效掩码字段，直接返回
        if (!hasSet) return 0;

        // 6. 执行更新
        // 第一个参数必须传 null，否则会触发 MP 默认的实体更新逻辑（全量更新），导致 Mask 失效
        return update(updateWrapper);
    }
}
