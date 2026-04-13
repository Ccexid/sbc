package me.link.bootstrap.modules.system.domain.model.entity;

import lombok.Data;
import lombok.Getter;
import me.link.bootstrap.shared.kernel.enums.StatusEnum;
import me.link.bootstrap.shared.util.SystemClockUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * 绑定域名数组 (由 JSON 映射)
     */
    private List<String> websites;

    // --- 审计元数据 (仅在基础设施层设置) ---
    @Getter
    private LocalDateTime createTime;
    @Getter
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
     *
     * @return true-已过期，false-未过期或无过期时间
     */
    public boolean isExpired() {
        return expireTime != null && expireTime.isBefore(SystemClockUtil.localDateTime());
    }

    /**
     * 校验是否可以创建新账号
     * <p>
     * 判断条件：
     * <ul>
     *   <li>账号数量限制必须大于0</li>
     *   <li>当前账号数未达到上限</li>
     * </ul>
     *
     * @param currentAccountCount 当前已存在的账号总数，必须为非负数
     * @return true-有配额可创建，false-已达上限或参数非法
     */
    public boolean hasAccountQuota(long currentAccountCount) {
        if (currentAccountCount < 0) {
            return false;
        }
        return accountCount != null && accountCount > 0 && currentAccountCount < accountCount;
    }

    /**
     * 禁用租户
     * <p>
     * 将租户状态设置为停用，后续操作将被拒绝
     */
    public void disable() {
        this.status = StatusEnum.DISABLE;
    }

    /**
     * 启用租户
     * <p>
     * 将租户状态恢复为正常
     */
    public void enable() {
        this.status = StatusEnum.NORMAL;
    }

    /**
     * 延长租户有效期
     * <p>
     * 仅当新过期时间晚于当前过期时间时才执行更新。
     * 如果当前无过期时间，则新时间必须为未来时间。
     *
     * @param newExpireTime 新的过期时间，不能为null
     * @throws IllegalArgumentException 如果新过期时间无效
     */
    public void extendExpiry(LocalDateTime newExpireTime) {
        if (newExpireTime == null) {
            throw new IllegalArgumentException("过期时间不能为空");
        }
        LocalDateTime now = SystemClockUtil.localDateTime();
        if (!newExpireTime.isAfter(now)) {
            throw new IllegalArgumentException("过期时间必须为未来时间");
        }
        if (this.expireTime != null && !newExpireTime.isAfter(this.expireTime)) {
            throw new IllegalArgumentException("新过期时间必须晚于当前过期时间");
        }
        this.expireTime = newExpireTime;
    }

    /**
     * 获取绑定的域名列表（防御性拷贝）
     * <p>
     * 返回不可修改的副本，防止外部直接修改内部状态
     *
     * @return 域名列表的不可修改视图
     */
    public List<String> getWebsites() {
        return websites != null ? Collections.unmodifiableList(websites) : Collections.emptyList();
    }

    /**
     * 设置绑定的域名列表（防御性拷贝）
     * <p>
     * 内部保存副本，防止外部修改影响内部状态
     *
     * @param websites 域名列表
     */
    public void setWebsites(List<String> websites) {
        this.websites = websites != null ? new ArrayList<>(websites) : null;
    }
}