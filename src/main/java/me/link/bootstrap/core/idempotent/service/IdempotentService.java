package me.link.bootstrap.core.idempotent.service;

import lombok.RequiredArgsConstructor;
import me.link.bootstrap.core.tenant.TenantContextHolder;
import me.link.bootstrap.core.utils.CacheUtils;
import me.link.bootstrap.core.utils.IdempotentSignUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotentService {

    private static final String TOKEN_PREFIX = "idempotent:token:";

    @Value("${app.idempotent.salt:LinkStart_2026}") // 建议配置文件注入，保持私密
    private String salt;

    public String createToken() {
        String tenantId = TenantContextHolder.getTenantId();
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 1. 对 (UUID + 租户ID) 进行加签
        String signature = IdempotentSignUtils.sign(uuid + tenantId, salt);

        // 2. 组合成对外公开的 Token: uuid.signature
        String publicToken = uuid + "." + signature;

        // 3. Redis Key 只存 UUID 部分，减少存储压力
        String redisKey = TOKEN_PREFIX + tenantId + ":" + uuid;
        CacheUtils.set(redisKey, "1", Duration.ofHours(1));

        return publicToken;
    }
}