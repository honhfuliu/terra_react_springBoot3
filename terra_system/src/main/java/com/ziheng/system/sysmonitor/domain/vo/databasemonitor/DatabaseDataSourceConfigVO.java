package com.ziheng.system.sysmonitor.domain.vo.databasemonitor;

import lombok.Data;

/**
 * 数据库数据源配置VO
 */
@Data
public class DatabaseDataSourceConfigVO {
    /** 数据源名称 */
    private String dataSourceName;

    /** 最大连接数 */
    private Integer maxActive;

    /** 最小空闲连接数 */
    private Integer minIdle;

    /** 获取连接的最大等待时间，单位毫秒 */
    private Long maxWaitMs;

    /** 慢 SQL 阈值，单位毫秒 */
    private Long slowSqlThresholdMs;
}
