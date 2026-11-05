package com.ziheng.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 启用状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum {

    DISABLED("0", "禁用"),
    ENABLED("1", "正常");

    private final String code;
    private final String description;

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (EnableStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDescription();
            }
        }
        return "未知";
    }
}
