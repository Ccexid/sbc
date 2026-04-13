package me.link.bootstrap.modules.system.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.shared.kernel.annotation.OperationLog;
import me.link.bootstrap.shared.kernel.constant.GlobalConstants;
import me.link.bootstrap.shared.kernel.enums.OperationEnum;
import me.link.bootstrap.shared.kernel.pojo.SortablePageParam;
import me.link.bootstrap.modules.system.application.dto.vo.TenantExpiryRespVO;
import me.link.bootstrap.modules.system.infrastructure.persistence.po.TenantPO;
import me.link.bootstrap.modules.system.application.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GlobalConstants.API_PREFIX + "/system/tenants")
@Validated
@RequiredArgsConstructor
@Tag(name = "租户接口")
public class TenantController {
    private final TenantService tenantService;

    @GetMapping
    @Operation(summary = "查询列表", parameters = {
            @Parameter(name = "pageNo", description = "页码", required = true),
            @Parameter(name = "pageSize", description = "每页数量", required = true),
            @Parameter(name = "sort", description = "排序字段")
    })
    @OperationLog(module = "租户接口", operation = OperationEnum.VIEWED, description = "查询列表")
    public ResponseEntity<IPage<TenantPO>> page(@Valid SortablePageParam param) {
        return ResponseEntity.ok(tenantService.searchByPage(param));
    }

    @GetMapping("/{id}:check-expired")
    @Operation(summary = "检查租户是否过期")
    public ResponseEntity<TenantExpiryRespVO> checkExpired(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.isExpired(id));
    }
}
