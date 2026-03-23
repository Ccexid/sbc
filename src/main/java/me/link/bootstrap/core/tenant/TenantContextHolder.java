package me.link.bootstrap.core.tenant;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 租户上下文持有者
 * 用于在线程间传递和存储当前租户的 ID 信息，支持线程池异步传递
 */
public class TenantContextHolder {

    /**
     * 存储当前线程的租户 ID
     * 使用 TransmittableThreadLocal 确保在线程池复用场景下也能正确传递租户上下文
     */
    private static final ThreadLocal<String> TENANT_ID = new TransmittableThreadLocal<>();

    /**
     * 标记位：指示当前操作是否忽略租户隔离
     * 当设置为 true 时，数据访问层将跳过租户条件的过滤
     */
    private static final ThreadLocal<Boolean> IGNORE_TENANT = new TransmittableThreadLocal<>();

    /**
     * 设置当前线程的租户 ID
     * @param tenantId 租户标识
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前线程的租户 ID
     * @return 租户标识，若未设置则返回 null
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除当前线程的租户上下文信息
     * 必须在请求结束或线程归还线程池前调用，防止内存泄漏和数据污染
     */
    public static void clear() {
        TENANT_ID.remove();
        IGNORE_TENANT.remove();
    }

    /**
     * 设置是否忽略租户隔离标记
     * @param ignore true 表示忽略租户隔离，false 表示启用租户隔离
     */
    public static void setIgnore(boolean ignore) {
        IGNORE_TENANT.set(ignore);
    }

    /**
     * 判断当前是否处于忽略租户隔离状态
     * @return true 表示忽略租户隔离，false 表示正常进行租户隔离
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE_TENANT.get());
    }
}