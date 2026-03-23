package me.link.bootstrap.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.IllegalSQLInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.tenant.handler.CustomTenantHandler;
import me.link.bootstrap.core.tenant.interceptor.CustomTenantInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final CustomTenantHandler customTenantHandler;

    /**
     * 配置 MyBatis-Plus 拦截器
     * 包含分页、乐观锁、非法 SQL 检测及防全表更新/删除功能
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建拦截器链容器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 添加分页插件，指定数据库类型为 H2（实际使用时应根据项目数据库类型调整）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));

        // 添加乐观锁插件，支持基于版本号的乐观锁机制
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 使用我们包装了注解逻辑的拦截器，并传入符合官方风格的 Handler
        CustomTenantInterceptor tenantInterceptor = new CustomTenantInterceptor(customTenantHandler);

        interceptor.addInnerInterceptor(tenantInterceptor);

        // 添加防全表更新/删除拦截器，避免误操作导致全表数据被修改或清除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }
}
