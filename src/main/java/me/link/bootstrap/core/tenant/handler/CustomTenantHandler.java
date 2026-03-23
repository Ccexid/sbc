package me.link.bootstrap.core.tenant.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.springframework.stereotype.Component;

/**
 * 适配官方 TenantLineHandler 接口的自定义处理器
 */
@Component
@RequiredArgsConstructor
public class CustomTenantHandler implements TenantLineHandler {

    @Override
    public Expression getTenantId() {
        // 1. 核心优化：如果 ThreadLocal 标记了忽略（通过 Service/Controller 注解设置）
        // 则此处返回 null 或特定处理，但在拦截器层级我们会直接跳过 SQL 改写
        String tenantIdStr = TenantContextHolder.getTenantId();

        if (tenantIdStr == null) {
            return new NullValue();
        }

        // 假设数据库 tenant_id 是 bigint 类型，使用 LongValue
        // 如果是 String 类型，则使用 new StringValue(tenantIdStr)
        return new LongValue(tenantIdStr);
    }

    @Override
    public String getTenantIdColumn() {
        // 对应数据库中的列名
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 这里可以配置哪些表永远不需要租户过滤（如系统公共表）
        return false;
    }
}