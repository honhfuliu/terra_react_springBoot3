package com.ziheng.system.sysloginlog.domain.vo;

import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import lombok.Data;

import java.util.Date;

/**
 * 登录日志列表行
 */
@Data
public class SysLoginLogVo {

    /**
     * 主键（前端表格 row-key）
     */
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * 登录位置
     */
    private String loginLocation;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态：1成功 0失败（CHAR(1)）
     */
    private String status;

    /**
     * 操作信息
     */
    private String message;

    /**
     * 登录时间（yyyy-MM-dd HH:mm:ss）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date loginTime;

    public static SysLoginLogVo from(SysLoginLog log) {
        SysLoginLogVo vo = new SysLoginLogVo();
        vo.setId(log.getId());
        vo.setUsername(log.getUsername());
        vo.setIpAddress(log.getIpAddress());
        vo.setLoginLocation(log.getLoginLocation());
        vo.setBrowser(log.getBrowser());
        vo.setOs(log.getOs());
        vo.setStatus(log.getStatus());
        vo.setMessage(log.getMessage());
        vo.setLoginTime(log.getLoginTime());
        return vo;
    }
}
