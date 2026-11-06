package com.ziheng.system.sysdictdata.domain.enums;

import lombok.Getter;

/**
 * 字典数据是否默认
 * Y-是 N-否
 */
@Getter
public enum DictDefault {
    YES("Y", "是"),
    NO("N", "否");

    private final String code;
    private final String description;

    DictDefault(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (DictDefault item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
