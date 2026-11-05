package com.ziheng.common.core.enums;

import lombok.Getter;

@Getter
public enum ExecuteStatus {
    FAIL("0", "失败"),
    SUCCESS("1", "成功");

    private final String code;
    private final String desc;

    ExecuteStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (ExecuteStatus item : values()) {
            if (item.getCode().equals(code)) {
                return item.getDesc();
            }
        }
        return "未知";
    }

}
