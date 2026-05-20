package me.link.bootstrap.infrastructure.persistence.handler; // 💡 建议放至基础设施层的持久化 handler 包

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

import static me.link.bootstrap.shared.kernel.constant.GlobalConstants.*;

/**
 * 默认数据库公共字段自动填充处理器
 * <p>
 * 基于 MyBatis-Plus 官方推荐的严格模式（Strict Mode）实现，
 * 完美支持单表插入、Wrapper 更新及各种复杂条件下的时间与操作人填充。
 * </p>
 *
 * @author Cceixd
 */
@Slf4j
public class LinkDefaultDBFieldHandler implements MetaObjectHandler {

    /**
     * 插入操作时的字段填充逻辑
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentUserId = getCurrentUserId();

        // 1. 严格模式插入时间：只有当实体类中该字段为 null 时才会填充，避免覆盖开发者手动传入的特定时间
        this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, now);
        this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);

        // 2. 严格模式插入操作人
        if (currentUserId != null) {
            this.strictInsertFill(metaObject, CREATOR, String.class, currentUserId);
            this.strictInsertFill(metaObject, UPDATER, String.class, currentUserId);
        }

        if (log.isDebugEnabled()) {
            log.debug("插入填充完成: createTime={}, updateTime={}, creator={}, updater={}",
                    now, now, currentUserId, currentUserId);
        }
    }

    /**
     * 更新操作时的字段填充逻辑
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String currentUserId = getCurrentUserId();

        // 3. 严格模式更新时间：MyBatis-Plus 默认策略是如果字段为 null 则填充。
        this.strictUpdateFill(metaObject, UPDATE_TIME, LocalDateTime.class, now);

        // 4. 更新操作人：使用 strictUpdateFill 确保语义正确
        if (currentUserId != null) {
            this.strictUpdateFill(metaObject, UPDATER, String.class, currentUserId);
        }

        if (log.isDebugEnabled()) {
            log.debug("更新填充完成: updateTime={}, updater={}", now, currentUserId);
        }
    }

    /**
     * 获取当前登录用户的 ID 编号
     *
     * @return 当前操作人 ID，未登录或系统异步任务返回 "SYSTEM"
     */
    private String getCurrentUserId() {
        try {
            // 💡 结合你项目中引入的 Sa-Token 框架获取：
            if (StpUtil.isLogin()) {
                String loginId = StpUtil.getLoginIdAsString();
                if (log.isTraceEnabled()) {
                    log.trace("获取到登录用户ID: {}", loginId);
                }
                return loginId;
            }
            return SYSTEM_USER; // 兜底默认值，表示系统操作
        } catch (Exception e) {
            // 规避定时任务、异步线程池或 MQ 消费时无 Web 上下文导致的报错
            log.warn("获取当前登录用户ID失败，使用SYSTEM作为默认值。异常信息: {}", e.getMessage());
            return SYSTEM_USER;
        }
    }
}
