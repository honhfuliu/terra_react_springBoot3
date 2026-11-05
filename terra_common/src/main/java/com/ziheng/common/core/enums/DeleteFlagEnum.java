package com.ziheng.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除标记枚举
 * 0-存在 1-删除
 */
@Getter
@AllArgsConstructor
public enum DeleteFlagEnum {

    EXIST("0", "存在"),
    DELETED("1", "删除");

    private final String code;
    private final String description;
}
