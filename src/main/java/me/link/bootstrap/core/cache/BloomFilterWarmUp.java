package me.link.bootstrap.core.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.core.log.entity.AuditLogEntity;
import me.link.bootstrap.core.log.mapper.AuditLogMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 布隆过滤器预热任务
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BloomFilterWarmUp implements CommandLineRunner {

    private final BloomFilterService bloomFilterService;
    private final AuditLogMapper auditLogMapper; // 注入你的业务 Mapper

    @Override
    public void run(String... args) {
        log.info(">>> 开始预热布隆过滤器...");

        // 1. 初始化过滤器：预期 100 万数据，3% 误判率
        var filter = bloomFilterService.createOrGetFilter(1000000L, 0.03);

        // 2. 从数据库加载关键 ID（例如加载所有有效的业务 ID）
        // 注意：生产环境应分批查询，防止 OOM
        try {
            // 模拟从数据库获取热点数据的 ID 列表
            List<String> businessIds = auditLogMapper.selectList(new LambdaQueryWrapper<>()).stream().map(AuditLogEntity::getBusinessId).toList();

            if (businessIds != null && !businessIds.isEmpty()) {
                businessIds.forEach(filter::add);
                log.info(">>> 布隆过滤器预热完成，共加载 {} 条数据", businessIds.size());
            }
        } catch (Exception e) {
            log.error(">>> 布隆过滤器预热失败", e);
        }
    }
}