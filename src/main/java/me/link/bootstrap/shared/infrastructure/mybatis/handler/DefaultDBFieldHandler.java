package me.link.bootstrap.shared.infrastructure.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.shared.infrastructure.mybatis.domain.BaseDO;
import me.link.bootstrap.shared.util.SystemClockUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 默认数据库字段填充处理器
 * 实现 MyBatis-Plus 的 MetaObjectHandler 接口，用于自动填充创建时间、更新时间及自定义主键
 */
@Component
@Slf4j
public class DefaultDBFieldHandler implements MetaObjectHandler {

    /**
     * 插入操作时的字段填充逻辑
     * 1. 若对象继承自 BaseDO，自动填充创建时间和更新时间（如果为空）
     *
     * @param metaObject MyBatis 元数据对象，包含待插入的实体对象信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime current = SystemClockUtil.localDateTime();
        // 处理基础领域对象的时间字段
        if (metaObject.getOriginalObject() instanceof BaseDO baseDO) {
            // 创建时间为空，则以当前时间为插入时间
            if (Objects.isNull(baseDO.getCreateTime())) {
                baseDO.setCreateTime(current);
            }
            // 更新时间为空，则以当前时间为更新时间
            if (Objects.isNull(baseDO.getUpdateTime())) {
                baseDO.setUpdateTime(current);
            }
        }
    }

    /**
     * 更新操作时的字段填充逻辑
     * 目前仅自动填充更新时间（如果需要可扩展其他逻辑）
     *
     * @param metaObject MyBatis 元数据对象，包含待更新的实体对象信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime current = SystemClockUtil.localDateTime();

        // 若对象继承自 BaseDO，自动填充更新时间
        if (metaObject.getOriginalObject() instanceof BaseDO baseDO) {
            if (Objects.isNull(baseDO.getUpdateTime())) {
                baseDO.setUpdateTime(current);
            }
        }
    }
}
