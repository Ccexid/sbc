package me.link.bootstrap.core.cache;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BloomFilterService {

    private final RedissonClient redissonClient;
    private static final String FILTER_NAME = "bloom:data:exists";

    /**
     * 初始化布隆过滤器
     */
    public RBloomFilter<String> createOrGetFilter(long expectedInsertions, double falseProbability) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(FILTER_NAME);
        // 如果未初始化，则初始化
        bloomFilter.tryInit(expectedInsertions, falseProbability);
        return bloomFilter;
    }

    public void add(String value) {
        redissonClient.getBloomFilter(FILTER_NAME).add(value);
    }

    public boolean contains(String value) {
        return redissonClient.getBloomFilter(FILTER_NAME).contains(value);
    }
}