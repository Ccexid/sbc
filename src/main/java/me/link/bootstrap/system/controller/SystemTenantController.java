package me.link.bootstrap.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.common.Result;
import me.link.bootstrap.system.tenant.entity.SystemTenant;
import me.link.bootstrap.system.tenant.service.ISystemTenantService;
import me.link.bootstrap.system.tenant.vo.TenantPageReqVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01.租户管理")
@RestController
@RequestMapping("/system/tenants")
@RequiredArgsConstructor
public class SystemTenantController {

    private final ISystemTenantService tenantService;

    @Operation(summary = "新增租户 (P2S2B2C)")
    @PostMapping // 2. POST 语义即为“创建”
    public Result<Long> createTenant(@Valid @RequestBody SystemTenant tenant) {
        return Result.success(tenantService.createTenant(tenant));
    }

    @Operation(summary = "获取租户详情")
    @GetMapping("/{id}") // 3. 使用路径变量定位资源
    public Result<SystemTenant> getTenant(
            @Parameter(description = "租户ID", required = true)
            @PathVariable("id") Long id) {
        return Result.success(tenantService.getById(id));
    }

    @Operation(summary = "更新租户信息")
    @PutMapping("/{id}") // 4. PUT 语义为“全量更新”资源
    public Result<Boolean> updateTenant(@PathVariable("id") Long id, @RequestBody SystemTenant tenant) {
        tenant.setId(id); // 确保 ID 一致
        return Result.success(tenantService.updateById(tenant));
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{id}") // 5. DELETE 语义为“删除”资源
    public Result<Boolean> deleteTenant(@PathVariable("id") Long id) {
        return Result.success(tenantService.removeById(id));
    }

    @Operation(summary = "条件分页查询租户列表")
    @GetMapping("/page") // RESTful 习惯：列表查询通常用 GET
    public Result<IPage<SystemTenant>> getTenantPage(@Valid TenantPageReqVO pageReqVO) {
        return Result.success(tenantService.getTenantPage(pageReqVO));
    }
}