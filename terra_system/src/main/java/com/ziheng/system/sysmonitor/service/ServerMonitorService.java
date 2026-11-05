package com.ziheng.system.sysmonitor.service;

import com.ziheng.system.sysmonitor.domain.vo.servermonitor.ServerMonitorOverviewVO;


/**
 * 服务器监控服务
 */
public interface ServerMonitorService {

    ServerMonitorOverviewVO getOverview();
}
