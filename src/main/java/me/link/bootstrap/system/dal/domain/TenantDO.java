package me.link.bootstrap.system.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import me.link.bootstrap.core.domain.BaseDO;
import me.link.bootstrap.core.enums.StatusEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

import static me.link.bootstrap.core.constants.GlobalApiConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 租户实体对象
 * 继承自 BaseDO 以复用审计字段 (createTime, updateTime, creator, updater, deleted)
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "system_tenant", autoResultMap = true)
public class TenantDO extends BaseDO {

    /**
     * 租户编号 (主键 ID)
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户名
     */
    private String name;

    /**
     * 联系人的用户编号
     */
    private Long contactUserId;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系手机
     */
    private String contactMobile;

    /**
     * 租户状态 (0正常 1停用)
     */
    private StatusEnum status;

    /**
     * 绑定域名数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> websites;

    /**
     * 租户套餐编号
     */
    private Long packageId;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;

    /**
     * 账号数量
     */
    private Integer accountCount;

}
