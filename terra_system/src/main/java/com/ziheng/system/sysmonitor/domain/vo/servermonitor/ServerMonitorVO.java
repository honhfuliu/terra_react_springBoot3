package com.ziheng.system.sysmonitor.domain.vo.servermonitor;

import lombok.Data;

@Data
public class ServerMonitorVO {

    /**
     * CPU 逻辑核心数
     */
    private int cpuCores;

    /**
     * CPU 使用率（百分比，0~100）
     */
    private double cpuUsage;

    /**
     * 系统内存已用（GB）
     */
    private double memoryUsed;

    /**
     * 系统内存总量（GB）
     */
    private double memoryTotal;

    /**
     * 系统内存使用率（百分比）
     */
    private double memoryUsage;

    /**
     * 磁盘已用（GB）
     */
    private double diskUsed;

    /**
     * 磁盘总量（GB）
     */
    private double diskTotal;

    /**
     * 磁盘使用率（百分比）
     */
    private double diskUsage;

}
