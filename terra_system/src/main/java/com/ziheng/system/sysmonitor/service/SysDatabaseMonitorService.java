package com.ziheng.system.sysmonitor.service;


import com.ziheng.system.sysmonitor.domain.vo.databasemonitor.DatabaseMonitorVO;

public interface SysDatabaseMonitorService {
    /**
     * 获取数据库监控概览
     * @return 数据库监控概览
     */
    DatabaseMonitorVO getDatabaseMonitor();
}
