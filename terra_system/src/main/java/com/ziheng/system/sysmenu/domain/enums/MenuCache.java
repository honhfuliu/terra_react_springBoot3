package com.ziheng.system.sysmenu.domain.enums;

import lombok.Getter;

/**
 * 菜单是否缓存
 * 0-不缓存 1-缓存
 */
@Getter
public enum MenuCache {
    NO_CACHE("0", "不缓存"),
    CACHE("1", "缓存");

    private final String code;
    private final String description;

    MenuCache(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (MenuCache item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
