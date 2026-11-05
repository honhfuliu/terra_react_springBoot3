package com.ziheng.framework.cache;

import lombok.Getter;

import java.time.Duration;

@Getter
public enum CacheKey {
    /**
     * 字典数据
     */
    DICT("dict", Duration.ofMinutes(30)),

    /**
     * 用户权限
     */
    USER_PERMISSION("user:permission", Duration.ofMinutes(30)),

    /**
     * 用户菜单
     */
    USER_MENU("user:menu", Duration.ofMinutes(30)),

    /**
     * 部门树
     */
    DEPT_TREE("dept:tree", Duration.ofHours(2));

    /**
     * Key 前缀
     */
    private final String prefix;

    /**
     * 缓存存活时间
     */
    private final Duration ttl;

    CacheKey(String prefix, Duration ttl) {
        this.prefix = prefix;
        this.ttl = ttl;
    }
}
