package me.link.bootstrap.system.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;
import me.link.bootstrap.core.domain.TenantBaseDO;
import me.link.bootstrap.core.enums.OperationEnum;
import me.link.bootstrap.system.dal.enums.PlatformEnum;

import java.util.Map;

/**
 * 操作日志记录 实体类
 * 对应数据库表：system_operate_log
 */
@TableName(value = "system_operate_log", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OperateLogDO extends TenantBaseDO {
    /**
     * 自增编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 链路追踪编号
     */
    private String traceId;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户类型
     */
    private PlatformEnum userType;

    /**
     * 用户 IP
     */
    private String userIp;

    /**
     * 浏览器 UA
     */
    private String userAgent;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     *
     * @see OperationEnum
     */
    private OperationEnum operation;

    /**
     * 业务主键编号
     */
    private Long bizId;

    /**
     * 操作内容/变更详情
     */
    private String action;

    /**
     * 拓展字段
     * 注意：使用 JSON 类型需要指定 TypeHandler 并在 @TableName 开启 autoResultMap
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    /**
     * 操作结果 (1:成功, 0:失败)
     */
    private Boolean success;

    /**
     * 请求方法名
     */
    private String requestMethod;

    /**
     * 请求地址
     */
    private String requestUrl;

    /**
     * 执行耗时(ms)
     */
    private Integer duration;
}
