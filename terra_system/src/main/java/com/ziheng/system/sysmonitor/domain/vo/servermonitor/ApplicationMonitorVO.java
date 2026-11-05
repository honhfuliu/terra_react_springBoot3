package com.ziheng.system.sysmonitor.domain.vo.servermonitor;

import lombok.Data;

@Data
public class ApplicationMonitorVO {
    /** 应用名称 */
    private String applicationName;

    /** 当前激活环境 */
    private String activeProfile;

    /** 服务端口 */
    private int port;

    /** 应用运行状态 */
    private String status;

    /** 应用运行时长，毫秒 */
    private long uptime;

    /** 累计 HTTP 请求数 */
    private long totalRequests;

    /** 平均响应时间，毫秒 */
    private double averageResponseTime;
}
