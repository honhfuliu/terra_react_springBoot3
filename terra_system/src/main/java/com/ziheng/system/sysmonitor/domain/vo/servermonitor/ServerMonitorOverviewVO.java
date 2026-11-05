package com.ziheng.system.sysmonitor.domain.vo.servermonitor;

import lombok.Data;

@Data
public class ServerMonitorOverviewVO {
    /**
     * 服务器资源
     */
    private ServerMonitorVO server;

    /**
     * JVM 状态
     */
    private JvmMonitorVO jvm;

    /**
     * 应用服务
     */
    private ApplicationMonitorVO application;
}
