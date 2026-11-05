package com.ziheng.framework.cache;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Collection;

/**
 * Redis缓存服务
 */
public interface RedisCacheService {
    /**
     * 获取缓存
     */
    <T> T get(CacheKey cacheKey, String key, Class<T> clazz);

    /**
     * 获取泛型缓存
     */
    <T> T get(CacheKey cacheKey, String key, TypeReference<T> typeReference);

    /**
     * 写入缓存，使用 CacheKey 默认 TTL
     */
    void set(CacheKey cacheKey, String key, Object value);

    /**
     * 写入缓存，自定义 TTL
     */
    void set(CacheKey cacheKey, String key, Object value, Duration timeout);

    /**
     * 删除缓存
     */
    void delete(CacheKey cacheKey, String key);

    /**
     * 批量删除缓存
     */
    void delete(CacheKey cacheKey, Collection<String> keys);

    /**
     * 判断缓存是否存在
     */
    boolean exists(CacheKey cacheKey, String key);

}
