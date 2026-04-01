package me.link.bootstrap.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.constants.GlobalApiConstants;
import me.link.bootstrap.core.exception.GlobalException;
import me.link.bootstrap.system.dal.domain.TenantPackageDO;
import me.link.bootstrap.system.service.TenantPackageService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GlobalApiConstants.API_PREFIX + "/system/tenant-packages")
@Validated
@RequiredArgsConstructor
@Tag(name = "租户套餐")
public class TenantPackageController {
    private final TenantPackageService tenantPackageService;

    @PostMapping()
    @Operation(summary = "保存租户套餐")
    @Transactional(rollbackFor = GlobalException.class)
    public ResponseEntity<Boolean> save(@Validated @RequestBody TenantPackageDO tenantPackage) {
        return ResponseEntity.ok(tenantPackageService.save(tenantPackage));
    }
}
