package me.link.bootstrap.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.common.Result;
import me.link.bootstrap.core.lock.annotation.Lock;
import me.link.bootstrap.core.log.annotation.Log;
import me.link.bootstrap.system.tenant.entity.SystemTenant;
import me.link.bootstrap.system.tenant.service.ISystemTenantService;
import me.link.bootstrap.system.tenant.vo.TenantPageReqVO;
import org.springframework.web.bind.annotation.*;

/**
 * 租户管理控制层
 * 集成了审计日志与分布式锁保障
 */
@Tag(name = "01.租户管理")
@RestController
@RequestMapping("/system/tenants")
@RequiredArgsConstructor
public class SystemTenantController {

    private final ISystemTenantService tenantService;

    @Operation(summary = "新增租户 (P2S2B2C)")
    @PostMapping
    @Log(module = "租户管理",
            operation = "'新增租户: ' + #tenant.tenantName",
            isDiff = false)
    // 使用租户名称作为锁的 Key，防止短时间内创建同名租户
    @Lock(key = "'tenant:create:' + #tenant.tenantName", waitTime = 0)
    public Result<Long> createTenant(@Valid @RequestBody SystemTenant tenant) {
        return Result.success(tenantService.createTenant(tenant));
    }

    @Operation(summary = "更新租户信息")
    @PutMapping("/{id}")
    @Log(module = "租户管理",
            operation = "'修改租户信息'",
            businessId = "#id",
            serviceName = "systemTenantServiceImpl")
    // 锁定特定 ID，防止并发更新冲突
    @Lock(key = "'tenant:update:' + #id")
    public Result<Boolean> updateTenant(
            @PathVariable("id") Long id,
            @RequestBody SystemTenant tenant) {
        tenant.setId(id);
        return Result.success(tenantService.updateById(tenant));
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{id}")
    @Log(module = "租户管理",
            operation = "'删除租户'",
            businessId = "#id")
    // 删除操作必须加锁，防止多用户同时触发删除导致的逻辑异常
    @Lock(key = "'tenant:delete:' + #id")
    public Result<Boolean> deleteTenant(@PathVariable("id") Long id) {
        return Result.success(tenantService.removeById(id));
    }

    @Operation(summary = "获取租户详情")
    @GetMapping("/{id}")
    @Log(module = "租户管理", operation = "'查询租户详情: ' + #id", isDiff = false)
    public Result<SystemTenant> getTenant(
            @Parameter(description = "租户ID", required = true)
            @PathVariable("id") Long id) {
        return Result.success(tenantService.getById(id));
    }

    @Operation(summary = "条件分页查询租户列表")
    @GetMapping("/page")
    @Log(module = "租户管理", operation = "'分页查询租户列表'", isDiff = false)
    public Result<IPage<SystemTenant>> getTenantPage(@Valid TenantPageReqVO pageReqVO) {
        return Result.success(tenantService.getTenantPage(pageReqVO));
    }
}