package me.link.bootstrap.core.constants;

/**
 * 全局 API 常量类
 * <p>
 * 定义了系统中与 API 相关的全局常量，包括版本号、前缀和日期格式等
 * </p>
 */
public interface GlobalApiConstants {
    /**
     * API 版本号
     */
    String API_VERSION = "v1";

    /**
     * API 请求前缀路径
     * <p>
     * 由固定的 "/api/" 和版本号组成，用于统一 API 路由管理
     * </p>
     */
    String API_PREFIX = "/api/" + API_VERSION;

    /**
     * 日期时间格式化器 - 年月日格式
     * <p>
     * 格式示例：2024-01-15
     * </p>
     */
    String FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd";

    /**
     * 日期时间格式化器 - 完整时间格式
     * <p>
     * 格式示例：2024-01-15 10:30:45
     * </p>
     */
    String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = "yyyy-MM-dd HH:mm:ss";

    /**
     * 链路追踪 ID 的 HTTP 请求头名称
     * <p>
     * 用于在分布式系统中传递和追踪请求链路,格式示例:X-Trace-Id
     * </p>
     */
    String TRACE_ID_HEADER = "X-Trace-Id";
    
    /**
     * 应用项目名称
     */
    String APP_PROJECT_NAME = "bootstrap-link";
    
    /**
     * 序列号最大长度
     */
    int MAX_SEQ_LENGTH = 999999;
    
    /**
     * 日期时间格式化器 - 年月日时分格式
     * <p>
     * 格式示例:202401151030
     * </p>
     */
    String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE = "yyyyMMddHHmm";
    
    /**
     * 日期时间格式化器 - 年月日时分秒格式
     * <p>
     * 格式示例:20240115103045
     * </p>
     */
    String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_COMPACT = "yyyyMMddHHmmss";
    
    /**
     * 日期时间格式化器 - 年月日时分秒毫秒格式
     * <p>
     * 格式示例:20240115103045123
     * </p>
     */
    String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLI_COMPACT = "yyyyMMddHHmmssSSS";
    
    /**
     * 日期时间格式化器 - 年月日紧凑格式
     * <p>
     * 格式示例:20240115
     * </p>
     */
    String FORMAT_YEAR_MONTH_DAY_COMPACT = "yyyyMMdd";
}
