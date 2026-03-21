package me.link.bootstrap.core.idempotent.service;

import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.utils.CacheUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotentService {

    private static final String TOKEN_PREFIX = "idempotent:token:";

    /**
     * 创建幂等 Token
     * 格式: idempotent:token:{tenantId}:{uuid}
     */
    public String createToken() {
        String tenantId = TenantContextHolder.getTenantId();
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 构建带租户隔离的 Key
        String redisKey = TOKEN_PREFIX + tenantId + ":" + uuid;

        // 存入 Redis，设置 1 小时有效期
        CacheUtils.set(redisKey, uuid, Duration.ofHours(1));

        return uuid; // 返回给前端的只需 uuid 部分
    }
}