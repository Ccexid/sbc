package me.link.bootstrap.system.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.SuperBuilder;
import me.link.bootstrap.core.domain.BaseDO;
import me.link.bootstrap.core.enums.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @TableId(value = "id", type = IdType.AUTO)
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
     * 建议：如果数据库存的是逗号分隔的字符串，可以在此处保持 String，
     * 或者配合 MyBatis Plus 的 TypeHandler 自动转为 List<String>
     */
    private String website;

    /**
     * 租户套餐编号
     */
    private Long packageId;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    /**
     * 账号数量
     */
    private Integer accountCount;

}
