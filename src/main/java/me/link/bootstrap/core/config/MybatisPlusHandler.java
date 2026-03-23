package me.link.bootstrap.core.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 * 负责在插入和更新时自动注入公共字段：ID、时间、租户信息
 */
@Slf4j
@Component
public class MybatisPlusHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入自动填充...");
        LocalDateTime now = LocalDateTime.now();

        // 1. 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);

        // 2. 填充更新时间（初始值与创建时间一致）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 3. 填充租户 ID
        // 核心逻辑：从租户上下文中自动获取，避免在业务代码中手动 setTenantId
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            // 只有当实体类中有 tenantId 字段且当前值为空时才填充
            this.strictInsertFill(metaObject, "tenantId", Long.class, Long.valueOf(tenantId));
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新自动填充...");
        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}