package com.ziheng.system.sysmenu.domain.enums;

public enum MenuType {
    DIRECTORY("目录", "M"),
    MENU("菜单", "C"),
    BUTTON("按钮", "F"),
    LINK("外链", "L"),
    PERMISSION("权限", "P");

    private final String name;
    private final String code;

    MenuType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    /**
     * 判断菜单类型编码是否合法（M目录 / C菜单 / F按钮 / L外链 / P权限）
     */
    public static boolean isValid(String code) {
        for (MenuType item : values()) {
            if (item.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
