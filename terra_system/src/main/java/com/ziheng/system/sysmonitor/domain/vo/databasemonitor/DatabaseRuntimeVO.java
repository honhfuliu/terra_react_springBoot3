package com.ziheng.system.sysmonitor.domain.vo.databasemonitor;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据库运行时监控VO
 */
@Data
public class DatabaseRuntimeVO {
    /** 数据源名称 */
    private String dataSourceName;

    /** 连接池运行状态 */
    private String poolStatus;

    /** 平均 SQL 响应时间，单位毫秒 */
    private BigDecimal averageResponseTimeMs;

    /** 活跃连接数 */
    private Integer activeConnections;

    /** 空闲连接数 */
    private Integer idleConnections;

    /** 最大连接数 */
    private Integer maxConnections;

    /** 等待连接的线程数 */
    private Integer waitingThreads;

//    /** 近5分钟请求数 */
//    private Long requestCount;
//
//    /** 近5分钟请求成功率（百分比） */
//    private BigDecimal successRate;
//
//    /** 近5分钟平均请求耗时，单位毫秒 */
//    private BigDecimal averageRequestTimeMs;
//
//    /** 近5分钟慢查询数量 */
//    private Long slowQueryCount;
}
