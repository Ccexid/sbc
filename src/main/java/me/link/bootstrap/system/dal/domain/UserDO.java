package me.link.bootstrap.system.dal.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import me.link.bootstrap.core.domain.TenantBaseDO;
import me.link.bootstrap.core.enums.StatusEnum;
import me.link.bootstrap.system.dal.enums.PlatformEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static me.link.bootstrap.core.constants.GlobalApiConstants.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@TableName(value = "system_users", autoResultMap = true) // 必须开启 autoResultMap 以支持 TypeHandler
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserDO extends TenantBaseDO {

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 密码
     * 使用 JSON 序列化时通常需要忽略，或者手动处理加密
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 身份类型
     * 1:供应商S, 2:平台P, 3:商家B, 4:用户C
     */
    private PlatformEnum userType;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 帐号状态（0正常 1停用）
     */
    private StatusEnum status;

    /**
     * 所属组织ID（对应供应商ID或商家ID）
     * 建立 S/B 端业务关联的关键字段
     */
    private Long orgId;

    /**
     * 平台内部部门ID（仅P端使用）
     */
    private Long deptId;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime loginDate;
}
