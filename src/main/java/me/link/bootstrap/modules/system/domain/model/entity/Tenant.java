package me.link.bootstrap.modules.system.domain.model.entity;

import lombok.Data;
import lombok.Getter;
import me.link.bootstrap.shared.kernel.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 租户领域实体 - 聚合根
 * 负责租户生命周期管理及业务准入逻辑
 */
@Data
public class Tenant {

    /**
     * 租户编号
     */
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
     * 联系手机 (加密存储)
     */
    private String contactMobile;

    /**
     * 租户状态
     */
    private StatusEnum status;

    /**
     * 绑定域名数组 (由 JSON 映射)
     */
    private List<String> websites;

    /**
     * 套餐编号
     */
    private Long packageId;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 账号数量限制
     */
    private Integer accountCount;

    // --- 审计元数据 (通常在领域层也保留，用于业务判断) ---
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // --- 核心领域行为 (Domain Behaviors) ---

    /**
     * 校验租户是否有效 (未禁用且未过期)
     */
    public boolean isValid() {
        return isEnabled() && !isExpired();
    }

    /**
     * 是否已被禁用
     */
    public boolean isEnabled() {
        return StatusEnum.NORMAL.equals(this.status);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.isBefore(LocalDateTime.now());
    }

    /**
     * 校验是否可以创建新账号
     *
     * @param currentAccountCount 当前已存在的账号总数
     */
    public boolean canCreateAccount(long currentAccountCount) {
        return accountCount > 0 && currentAccountCount < accountCount;
    }

    /**
     * 禁用租户
     */
    public void disable() {
        this.status = StatusEnum.DISABLE;
    }

    /**
     * 延长租户有效期
     */
    public void extendExpiry(LocalDateTime newExpireTime) {
        if (newExpireTime != null && newExpireTime.isAfter(this.expireTime)) {
            this.expireTime = newExpireTime;
        }
    }
}