package com.ziheng.system.sysmonitor.domain.vo.servermonitor;

import lombok.Data;

@Data
public class JvmMonitorVO {
    /** 堆内存已用，MB */
    private double heapUsed;

    /** 堆内存最大值，MB */
    private double heapMax;

    /** 堆内存使用率，0~100 */
    private double heapUsage;

    /** 当前线程数 */
    private int threadCount;

    /** JVM 启动以来 GC 总次数 */
    private long gcCount;

    /** 非堆内存已用，MB */
    private double nonHeapUsed;

    /** JVM 运行时间，毫秒 */
    private long uptime;
}
