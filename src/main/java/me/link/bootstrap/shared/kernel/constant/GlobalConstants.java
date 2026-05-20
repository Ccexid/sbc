package me.link.bootstrap.shared.kernel.constant;

/**
 * 全局常量定义
 * <p>
 * 包含系统级别的全局常量，涵盖 API 版本、前缀以及链路追踪等相关配置。
 * </p>
 * <p>
 * <strong>注意：</strong>数据库实体字段名相关常量请放置于 {@link me.link.bootstrap.infrastructure.persistence.handler.DefaultDBFieldHandler}
 * 或专门的持久化层常量类中，避免与 API 层常量混淆。
 * </p>
 *
 * @author Ccexid
 */
public interface GlobalConstants {

    /* ==================== API 配置 ==================== */

    /**
     * API 版本号
     * <p>示例：所有 RESTful API 的路径格式为 /api/v1/xxx</p>
     */
    String API_VERSION = "v1";

    /**
     * API 基础路径前缀
     * <p>示例：/api/v1</p>
     */
    String API_PREFIX = "/api/" + API_VERSION;

    /* ==================== 链路追踪 ==================== */

    /**
     * TraceId 请求头名称，用于分布式链路追踪透传
     * <p>在 HTTP 请求头中使用，格式为：<code>X-Trace-Id</code></p>
     */
    String TRACE_ID_HEADER = "X-Trace-Id";

    /* ==================== 数据库实体公共字段 ==================== */

    /**
     * 实体创建时间字段名
     */
    String CREATE_TIME = "createTime";

    /**
     * 实体更新时间字段名
     */
    String UPDATE_TIME = "updateTime";

    /**
     * 实体创建者字段名
     */
    String CREATOR = "creator";

    /**
     * 实体更新者字段名
     */
    String UPDATER = "updater";

    /**
     * 默认系统操作者标识（用于无用户登录时的系统自动操作）
     */
    String SYSTEM_USER = "SYSTEM";
}
