package com.ziheng.framework.cache.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.framework.cache.CacheKey;
import com.ziheng.framework.cache.RedisCacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheServiceImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取缓存
     * @param cacheKey 缓存key
     * @param key 键
     * @param clazz 类型
     * @return 值
     * @param <T> 类型
     */
    @Override
    public <T> T get(CacheKey cacheKey, String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(buildKey(cacheKey, key));
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    /**
     * 获取缓存
     * @param cacheKey 缓存key
     * @param key 键
     * @param typeReference 类型
     * @return 值
     * @param <T> 类型
     */
    @Override
    public <T> T get(CacheKey cacheKey, String key, TypeReference<T> typeReference) {
        Object value = redisTemplate.opsForValue().get(buildKey(cacheKey, key));

        if (value == null) {
            return null;
        }

        // 这里后面需要结合你的 ObjectMapper 处理泛型
        return objectMapper.convertValue(value, typeReference);
    }

    /**
     * 写入缓存，使用 CacheKey 默认 TTL
     * @param cacheKey 缓存key
     * @param key 键
     * @param value 值
     */
    @Override
    public void set(CacheKey cacheKey, String key, Object value) {
        redisTemplate.opsForValue().set(buildKey(cacheKey, key), value, cacheKey.getTtl());
    }

    /**
     * 写入缓存，自定义 TTL
     * @param cacheKey 缓存key
     * @param key 键
     * @param value 值
     * @param timeout 超时时间
     */
    @Override
    public void set(CacheKey cacheKey, String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(buildKey(cacheKey, key), value, timeout);
    }

    /**
     * 删除缓存
     * @param cacheKey 缓存key
     * @param key 键
     */
    @Override
    public void delete(CacheKey cacheKey, String key) {
        redisTemplate.delete(buildKey(cacheKey, key));
    }

    /**
     * 批量删除缓存
     * @param cacheKey 缓存key
     * @param keys 键集合
     */
    @Override
    public void delete(CacheKey cacheKey, Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        Collection<String> redisKeys = keys.stream()
                .map(key -> buildKey(cacheKey, key))
                .toList();
        redisTemplate.delete(redisKeys);
    }

    /**
     * 判断缓存是否存在
     * @param cacheKey 缓存key
     * @param key 键
     * @return 是否存在
     */
    @Override
    public boolean exists(CacheKey cacheKey, String key) {
        return redisTemplate.hasKey(
                buildKey(cacheKey, key)
        );
    }
    /**
     * 构建 Redis Key
     *
     * @param cacheKey 缓存 key 类型
     * @param key      业务 key
     * @return Redis Key
     */
    private String buildKey(CacheKey cacheKey, String key) {
        return "terra:cache:" + cacheKey.getPrefix() + ":" + key;
    }
}
