package com.ziheng.system.sysmonitor.domain.vo.databasemonitor;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据库监控概览VO
 */
@Data
public class DatabaseMonitorOverviewVO {
    /** 实例运行模式，例如：单实例运行稳定 */
    private String instanceMode;

    /** 健康状态：HEALTHY、WARNING、CRITICAL */
    private String healthStatus;

    /** 连接池组件名称，例如：Druid */
    private String poolName;

    /** 连接池版本，例如：1.2.23 */
    private String poolVersion;

    /** 连接池状态：正常、警告、异常 */
    private String status;

    /** 当前唯一数据源名称 */
    private String dataSourceName;

    /** 连接使用率（百分比，例如 31.11） */
    private BigDecimal connectionUsagePercent;

    /** 活跃连接数 */
    private Integer activeConnections;

    /** 最大连接数 */
    private Integer maxConnections;

    /** 等待连接的线程数 */
    private Integer waitingThreads;

    /** 空闲连接数 */
    private Integer idleConnections;

    /** 近24小时慢查询数量 */
    private Long slowQueryCount;
}
