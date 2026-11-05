package com.ziheng.system.sysmonitor.service;

import com.ziheng.system.sysmonitor.domain.vo.redismonitor.RedisMonitorOverviewVO;

public interface SysRedisMonitorService {
    /**
     * 获取 Redis 监控概览
     * @return
     */
    RedisMonitorOverviewVO getRedisMonitorOverview();
}
