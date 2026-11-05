package com.ziheng.system.sysmonitor.domain.vo.databasemonitor;

import lombok.Data;

/**
 * 数据库监控总览
 */
@Data
public class DatabaseMonitorVO {

    /** 数据库监控概览 */
    private DatabaseMonitorOverviewVO overview;

    /** 数据库运行时数据 */
    private DatabaseRuntimeVO runtime;

    /** 数据库连接池健康度 */
    private DatabaseConnectionPoolHealthVO connectionPoolHealth;

    /** 数据源配置 */
    private DatabaseDataSourceConfigVO dataSourceConfig;
}
