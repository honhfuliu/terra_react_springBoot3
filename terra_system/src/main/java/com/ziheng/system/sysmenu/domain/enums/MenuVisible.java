package com.ziheng.system.sysmenu.domain.enums;

import lombok.Getter;

/**
 * 菜单是否可见
 * 0-隐藏 1-显示
 */
@Getter
public enum MenuVisible {
    HIDDEN("0", "隐藏"),
    VISIBLE("1", "显示");

    private final String code;
    private final String description;

    MenuVisible(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (MenuVisible item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
