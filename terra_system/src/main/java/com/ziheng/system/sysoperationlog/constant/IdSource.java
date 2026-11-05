package com.ziheng.system.sysoperationlog.constant;

/**
 * ID来源
 */
public enum IdSource {
    /**
     * 自动获取
     */
    AUTO,

    /**
     * 路径参数
     */
    PATH,

    /**
     * 请求参数
     */
    PARAM,

    /**
     * 请求体
     */
    BODY
}
