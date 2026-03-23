package me.link.bootstrap.core.tenant.interceptor;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.tenant.annotation.IgnoreTenant;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;

/**
 * 自定义多租户拦截器
 * 继承自 MyBatis-Plus 的 TenantLineInnerInterceptor，用于在 SQL 执行前动态注入租户条件或忽略租户过滤
 */
public class CustomTenantInterceptor extends TenantLineInnerInterceptor {

    // 缓存 Mapper 接口或方法是否标注了 @IgnoreTenant 注解，避免重复反射查询提升性能
    private static final Map<String, Boolean> CACHE = new ConcurrentReferenceHashMap<>();

    public CustomTenantInterceptor(TenantLineHandler tenantLineHandler) {
        super(tenantLineHandler);
    }

    /**
     * 在 SQL 语句准备执行前被调用，用于判断是否需要跳过租户过滤逻辑
     *
     * @param sh                 StatementHandler 处理器
     * @param connection         数据库连接
     * @param transactionTimeout 事务超时时间
     */
    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        // 获取 MyBatis-Plus 封装的 StatementHandler 工具类
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        // 获取当前执行的 MappedStatement 对象，包含 SQL 映射信息
        MappedStatement ms = mpSh.mappedStatement();

        // --- 核心优化逻辑开始：三级优先级判断是否忽略租户过滤 ---

        // 第一步：检查 ThreadLocal 中是否设置了忽略租户标识
        // 适用场景：在 Service 或 Controller 层通过 TenantContextHolder.setIgnore(true) 临时关闭租户过滤
        if (TenantContextHolder.isIgnore()) {
            return; // 直接返回，不注入租户条件
        }

        // 第二步：检查当前 Mapper 接口或其方法上是否标注了 @IgnoreTenant 注解
        // 适用场景：特定 Mapper 方法需要永久忽略租户隔离（如公共字典表查询）
        if (isAnnotatedIgnore(ms.getId())) {
            return; // 直接返回，不注入租户条件
        }

        // 第三步：检查 MyBatis-Plus 原生的拦截器忽略配置（通过 @InterceptorIgnore 或全局配置）
        // 适用场景：使用 MP 原生机制控制租户忽略
        if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) {
            return; // 直接返回，不注入租户条件
        }

        // --- 核心优化逻辑结束 ---

        // 若以上条件均未命中，则执行默认的租户 SQL 重写逻辑
        SqlCommandType sct = ms.getSqlCommandType();
        // 仅对增删改查四类操作进行租户条件注入
        if (sct == SqlCommandType.INSERT || sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE || sct == SqlCommandType.SELECT) {
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            // 调用 parserMulti 方法解析原始 SQL 并注入租户过滤条件（如 WHERE tenant_id = ?）
            // 该方法内部会调用自定义的 TenantLineHandler 实现来获取当前租户 ID
            mpBs.sql(this.parserMulti(mpBs.sql(), null));
        }
    }

    /**
     * 判断指定的 Mapper 方法（由 msId 标识）是否被 @IgnoreTenant 注解标记
     * 使用缓存机制避免重复反射开销
     *
     * @param msId MyBatis 的 mapped statement ID，格式为 "全限定类名。方法名"
     * @return 如果类或方法上有 @IgnoreTenant 注解则返回 true，否则 false
     */
    private boolean isAnnotatedIgnore(String msId) {
        return CACHE.computeIfAbsent(msId, key -> {
            try {
                // 分离类名和方法名：例如 "com.example.mapper.UserMapper.selectById" -> 类=com.example.mapper.UserMapper, 方法=selectById
                int lastDotIndex = key.lastIndexOf(".");
                Class<?> clazz = Class.forName(key.substring(0, lastDotIndex));
                String methodName = key.substring(lastDotIndex + 1);

                // 检查类级别是否有 @IgnoreTenant 注解
                if (clazz.isAnnotationPresent(IgnoreTenant.class)) {
                    return true;
                }

                // 检查方法级别是否有 @IgnoreTenant 注解
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(methodName) && m.isAnnotationPresent(IgnoreTenant.class)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // 反射失败时静默处理，默认不忽略租户
            }
            return false;
        });
    }
}