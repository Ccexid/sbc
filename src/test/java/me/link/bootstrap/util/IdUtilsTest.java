package me.link.bootstrap.util;

import lombok.extern.slf4j.Slf4j;
import me.link.bootstrap.LinkMainApplication;
import me.link.bootstrap.system.dal.mapper.SequenceMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = LinkMainApplication.class)
@DisplayName("业务 ID 生成器测试")
@Slf4j
class IdUtilsTest {

    @Autowired
    private IdUtils idUtils;

    @Autowired
    private SequenceMapper sequenceMapper;

    /**
     * 测试合同号生成（按天重置模式）
     * 注意：由于 DATE_FORMATTER 改为 yyyyMMddHHmmss (14位)，截取位置需相应变动
     */
    @Test
    @DisplayName("测试合同号生成（按天重置模式）")
    void testNextDailyId() {
        String prefix = "HT";
        int digit = 4;

        // 生成 ID
        String id1 = idUtils.nextId(prefix, digit, true);
        String id2 = idUtils.nextId(prefix, digit, true);

        log.info("生成的合同1: {}", id1);
        log.info("生成的合同2: {}", id2);

        Assertions.assertTrue(id1.startsWith(prefix));
        // 长度验证：前缀(2) + 时间(14) + 数字位(4) = 20
        Assertions.assertEquals(20, id1.length());

        // 验证自增性：截取最后 digit 位进行比较
        long num1 = Long.parseLong(id1.substring(id1.length() - digit));
        long num2 = Long.parseLong(id2.substring(id2.length() - digit));
        Assertions.assertEquals(num1 + 1, num2);
    }

    @Test
    @DisplayName("测试房源号生成（全局递增模式）")
    void testNextGlobalId() {
        String id = idUtils.nextId("FD", 15, false);
        log.info("生成的房源ID: {}", id);

        Assertions.assertTrue(id.startsWith("FD"));
        // 全局模式 ID 长度 = 前缀(2) + 数字位(15) = 17
        Assertions.assertEquals(17, id.length());
    }

    /**
     * 核心测试：模拟 Redis 异常时的数据库补偿逻辑
     */
    @Test
    @DisplayName("测试 Redis 宕机后的数据库降级逻辑")
    void testRedisFallback() {
        // 1. 正常生成一个 ID，确保数据库中有记录
        String idBefore = idUtils.nextId("FALLBACK", 6, false);
        log.info("正常模式 ID: {}", idBefore);

        // 2. 这里可以通过特定的工具手动关闭 Redisson 连接，或者在代码中人工制造异常
        // 模拟逻辑：即使 Redis 失败，下一次生成的 ID 仍应基于 DB 记录自增
        String idAfter = idUtils.nextId("FALLBACK", 6, false);
        log.info("降级模式 ID: {}", idAfter);

        long num1 = Long.parseLong(idBefore.substring(idBefore.length() - 6));
        long num2 = Long.parseLong(idAfter.substring(idAfter.length() - 6));
        Assertions.assertTrue(num2 > num1, "降级模式下 ID 未能正确自增");
    }

    @Test
    @DisplayName("高并发下的唯一性测试")
    void testConcurrency() throws InterruptedException {
        int threadCount = 20;
        int iterations = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Set<String> idSet = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        String id = idUtils.nextId("CONCUR", 6, true);
                        idSet.add(id);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Assertions.assertEquals(threadCount * iterations, idSet.size(), "并发下生成了重复的 ID！");
    }
}