package me.link.bootstrap.shared.kernel.repository;

import org.redisson.api.RBloomFilter;

public interface BloomRepository<T> {
    /**
     * 获取布隆过滤器实例
     *
     * @return 布隆过滤器实例
     */
    RBloomFilter<T> getBloomFilter();

    /**
     * 检查指定值是否不存在于布隆过滤器中
     * <p>
     * 如果布隆过滤器未初始化或值不在过滤器中，则返回true；
     * 如果值存在于过滤器中，则返回false
     *
     * @param value 待检查的值，泛型类型
     * @return true-布隆过滤器为空或值不存在；false-值存在于过滤器中
     */
    default boolean exists(T value) {
        RBloomFilter<T> bloomFilter = getBloomFilter();
        return bloomFilter == null || !bloomFilter.contains(value);
    }
}
