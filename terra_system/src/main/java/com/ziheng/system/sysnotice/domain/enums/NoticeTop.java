package com.ziheng.system.sysnotice.domain.enums;

import lombok.Getter;

/**
 * 公告是否置顶
 * 0-否 1-是
 */
@Getter
public enum NoticeTop {
    NO("0", "否"),
    YES("1", "是");

    private final String code;
    private final String description;

    NoticeTop(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (NoticeTop item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
