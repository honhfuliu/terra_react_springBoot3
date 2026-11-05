package com.ziheng.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举
 */
@Getter
@AllArgsConstructor
public enum GenderEnum {

    UNKNOWN("0", "未知"),
    MALE("1", "男"),
    FEMALE("2", "女");

    private final String code;
    private final String description;

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (GenderEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
