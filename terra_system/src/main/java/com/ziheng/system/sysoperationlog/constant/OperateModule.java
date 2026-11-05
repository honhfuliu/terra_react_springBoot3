package com.ziheng.system.sysoperationlog.constant;

import lombok.Getter;

@Getter
public enum OperateModule {
    USER("USER", "用户管理"),
    ROLE("ROLE", "角色管理"),
    DEPT("DEPT", "部门管理"),
    MENU("MENU", "菜单管理"),
    DICT("DICT", "字典管理"),
    LOGIN("LOGIN", "登录日志"),
    ANNOUNCEMENT("ANNOUNCEMENT", "公告管理"),
    OPERATION_LOG("OPERATION_LOG", "操作日志");

    private final String code;
    private final String name;

    OperateModule(String code, String name) {
        this.code = code;
        this.name = name;
    }
    /**
     * 根据编码获取描述
     */
    public static String getDescription(String code) {
        for (OperateModule item : values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return "未知";
    }

}
