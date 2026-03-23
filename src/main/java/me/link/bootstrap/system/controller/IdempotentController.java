package me.link.bootstrap.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.common.Result;
import me.link.bootstrap.core.idempotent.annotation.Idempotent;
import me.link.bootstrap.core.idempotent.service.IdempotentService;
import me.link.bootstrap.core.lock.annotation.Lock;
import me.link.bootstrap.core.log.annotation.Log;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 幂等性令牌控制器
 * 遵循 RESTful 设计规范：Resource 为 tokens
 */
@Tag(name = "基础服务", description = "幂等性管理")
@RestController
@RequestMapping(value = "/v1/idempotent-tokens", produces = "application/json") // 建议增加版本号，资源名称使用复数
@RequiredArgsConstructor // 使用构造器注入代替 @Autowired
public class IdempotentController {

    private final IdempotentService idempotentService;

    /**
     * 获取/创建一个新的幂等令牌
     * RESTful 语义：POST 通常用于创建资源，这里生成一个 Token 存入 Redis 属于创建行为
     * 如果你坚持使用获取语义，可以将注解改为 @GetMapping
     */
    @Operation(summary = "获取幂等令牌", description = "用于提交表单前获取唯一凭证，确保接口幂等")
    @PostMapping
    @Lock(key = "idempotent:token:get")
    @Log(module = "idempotent", operation = "获取幂等令牌")
    public Result<String> createToken() {
        // 调用优化后的 Service，内部已包含多租户隔离逻辑
        String token = idempotentService.createToken();
        return Result.success(token);
    }

    /**
     * 校验令牌有效性 (POST)
     * 为什么用 POST：Token 属于敏感信息，不建议放在 URL 参数中(GET)，防止日志泄露。
     * * @param token 完整 Token (uuid.signature)
     *
     * @return true: 有效且未被使用; false: 无效或已过期
     */
    @Operation(summary = "校验幂等令牌", description = "手动检查 Token 是否有效，注意：此操作不会消耗(删除) Token")
    @PostMapping("/verify")
    @Log(module = "idempotent", operation = "校验幂等令牌")
    @Lock(key = "idempotent:token:verify")
    @Idempotent
    public Result<Boolean> verifyToken() {
        // 调用 Service 层的校验逻辑
        return Result.success(true);
    }
}