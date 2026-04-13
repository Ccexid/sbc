package me.link.bootstrap.modules.system.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.shared.kernel.constant.GlobalConstants;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantPackagePO;
import me.link.bootstrap.modules.system.application.service.TenantPackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(GlobalConstants.API_PREFIX + "/system/tenant-packages")
@Validated
@RequiredArgsConstructor
@Tag(name = "租户套餐")
public class TenantPackageController {
    private final TenantPackageService tenantPackageService;

    @PostMapping
    @Operation(summary = "保存租户套餐")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Boolean> createTenantPackage(@Validated @RequestBody TenantPackagePO tenantPackage) {
        return ResponseEntity.ok(tenantPackageService.save(tenantPackage));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "更新租户套餐(局部)")
    public ResponseEntity<Boolean> patchUpdateTenantPackage(@PathVariable Long id,
                                                            @RequestBody TenantPackagePO tenantPackage) {
        tenantPackage.setId(id);
        return ResponseEntity.ok(tenantPackageService.updateById(tenantPackage));
    }

    @PostMapping("/{id}")
    @Operation(summary = "更新租户套餐(全量)")
    public ResponseEntity<Boolean> updateTenantPackage(@PathVariable Long id,
                                                       @RequestBody TenantPackagePO tenantPackage) {
        tenantPackage.setId(id);
        return ResponseEntity.ok(tenantPackageService.updateById(tenantPackage));
    }
}
