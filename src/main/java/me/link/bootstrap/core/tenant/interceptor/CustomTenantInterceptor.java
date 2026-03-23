package me.link.bootstrap.core.tenant.interceptor;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.tenant.annotation.IgnoreTenant;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;

/**
 * 修复版多租户拦截器
 */
@Slf4j
public class CustomTenantInterceptor extends TenantLineInnerInterceptor {

    private static final Map<String, Boolean> CACHE = new ConcurrentReferenceHashMap<>();

    public CustomTenantInterceptor(TenantLineHandler tenantLineHandler) {
        super(tenantLineHandler);
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        // 获取当前执行的方法对象
        MappedStatement ms = PluginUtils.mpStatementHandler(sh).mappedStatement();

        // 2. 检查标记位：这是最优先的判断
        // 如果子线程里 setIgnore(true) 成功，这里会直接 return
        if (TenantContextHolder.isIgnore()) {
            log.trace(">>> [Tenant] 检测到忽略标记，跳过 SQL 改写: {}", ms.getId());
            return;
        }

        // 2. 检查自定义注解 @IgnoreTenant
        if (isAnnotatedIgnore(ms.getId())) return;

        // 3. 检查 MP 原生忽略
        if (InterceptorIgnoreHelper.willIgnoreTenantLine(ms.getId())) return;

        // --- 核心修复点 ---
        // 只要通过了上面的忽略检查，就交给父类去执行一次标准的 SQL 改写
        // 不要在这里手动调用 mpBs.sql(...)，否则会导致重复注入
        super.beforePrepare(sh, connection, transactionTimeout);
    }

    private boolean isAnnotatedIgnore(String msId) {
        return CACHE.computeIfAbsent(msId, key -> {
            try {
                int lastDotIndex = key.lastIndexOf(".");
                Class<?> clazz = Class.forName(key.substring(0, lastDotIndex));
                String methodName = key.substring(lastDotIndex + 1);
                if (clazz.isAnnotationPresent(IgnoreTenant.class)) return true;
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(methodName) && m.isAnnotationPresent(IgnoreTenant.class)) return true;
                }
            } catch (Exception ignored) {
            }
            return false;
        });
    }
}