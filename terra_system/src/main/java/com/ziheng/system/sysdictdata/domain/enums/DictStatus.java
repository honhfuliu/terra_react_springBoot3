package com.ziheng.system.sysdictdata.domain.enums;

import lombok.Getter;

/**
 * 字典状态
 * 0-正常 1-停用
 */
@Getter
public enum DictStatus {
    NORMAL("0", "正常"),
    DISABLED("1", "停用");

    private final String code;
    private final String description;

    DictStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (DictStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
