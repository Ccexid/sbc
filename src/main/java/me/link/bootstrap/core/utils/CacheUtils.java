package me.link.bootstrap.core.utils;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * 基于 Redisson 的高性能缓存工具类 (JDK 17 优化版)
 */
@Component
public class CacheUtils {

    private final RedissonClient redissonClient;
    private static CacheUtils instance;

    public CacheUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        instance = this;
    }

    /**
     * 内部安全校验：确保静态实例已注入
     */
    private static void checkInstance() {
        Assert.notNull(instance, "CacheUtils 未初始化，请确保 Spring 容器已启动");
    }

    public static <T> T get(String key) {
        checkInstance();
        return instance.redissonClient.<T>getBucket(key).get();
    }

    /**
     * 使用指定的 set(V, Duration) 方法
     */
    public static <T> void set(String key, T value, Duration ttl) {
        if (value == null) return;
        checkInstance();
        instance.redissonClient.getBucket(key).set(value, ttl);
    }

    /**
     * 原子删除操作
     */
    public static boolean delete(String key) {
        checkInstance();
        return instance.redissonClient.getBucket(key).delete();
    }
}