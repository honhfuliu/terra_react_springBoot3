package com.ziheng.system.sysoperationlog.constant;

import lombok.Getter;

/**
 * 操作类型
 */
@Getter
public enum OperateType {
    INSERT("INSERT", "新增"),
    UPDATE("UPDATE", "修改"),
    INSERT_UPDATE("INSERT_UPDATE", "新增或修改"),
    DELETE("DELETE", "删除"),
    QUERY("QUERY", "查询"),
    EXPORT("EXPORT", "导出"),
    IMPORT("IMPORT", "导入");

    private final String code;
    private final String name;

    OperateType(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (OperateType item : values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "未知";
    }
}
