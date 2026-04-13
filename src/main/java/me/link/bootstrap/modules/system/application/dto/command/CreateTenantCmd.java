package me.link.bootstrap.modules.system.application.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建租户命令对象
 * 负责接收新增租户请求并进行参数校验
 */
@Data
@Schema(description = "创建租户命令")
public class CreateTenantCmd {

    @Schema(description = "租户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "极客链接有限公司")
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 64, message = "租户名称长度不能超过 64 个字符")
    private String name;

    @Schema(description = "联系人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "林某某")
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 32, message = "联系人姓名长度不能超过 32 个字符")
    private String contactName;

    @Schema(description = "联系手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系手机不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactMobile;

    @Schema(description = "套餐编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "必须选择一个套餐")
    private Long packageId;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "过期时间不能为空")
    @Future(message = "过期时间必须是一个将来时间")
    private LocalDateTime expireTime;

    @Schema(description = "账号数量限制", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "账号数量限制不能为空")
    @Min(value = 1, message = "账号额度至少为 1")
    private Integer accountCount;

    @Schema(description = "绑定域名数组")
    private List<String> websites;

    @Schema(description = "初始管理员密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "初始密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 位之间")
    private String adminPassword;
}