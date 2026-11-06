package com.ziheng.system.sysnotice.domain.enums;

import lombok.Getter;

/**
 * 公告类型
 * NOTICE-通知 ANNOUNCEMENT-公告
 */
@Getter
public enum NoticeType {
    NOTICE("NOTICE", "通知"),
    ANNOUNCEMENT("ANNOUNCEMENT", "公告");

    private final String code;
    private final String description;

    NoticeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (NoticeType item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
