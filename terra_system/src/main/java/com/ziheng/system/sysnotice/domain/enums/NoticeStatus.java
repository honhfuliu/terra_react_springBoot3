package com.ziheng.system.sysnotice.domain.enums;

import lombok.Getter;

/**
 * 公告状态
 * 1-待发布 2-已发布 3-已下线
 */
@Getter
public enum NoticeStatus {
    PENDING("1", "待发布"),
    PUBLISHED("2", "已发布"),
    OFFLINE("3", "已下线");

    private final String code;
    private final String description;

    NoticeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (NoticeStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
