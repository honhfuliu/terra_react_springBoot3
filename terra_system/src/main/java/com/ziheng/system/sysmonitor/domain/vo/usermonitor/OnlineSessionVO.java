package com.ziheng.system.sysmonitor.domain.vo.usermonitor;

import lombok.Data;

import java.util.Date;
@Data
public class OnlineSessionVO {
    /**
     * 登录账号
     */
    private String username;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 登录时间
     */
    private Date loginTime;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 登录地址
     */
    private String loginLocation;

    /**
     * Token 剩余有效时间，单位：秒
     */
    private Long tokenTimeout;
    /**
     * 登录日志 ID
     */
    private Long loginLogId;
}
