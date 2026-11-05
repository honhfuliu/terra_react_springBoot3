package com.ziheng.system.sysmonitor.domain.vo.redismonitor;

import lombok.Data;

import java.util.List;

@Data
public class RedisMonitorOverviewVO {

    /** 实例信息及资源指标 */
    private RedisInstanceVO instance;

    /** 缓存健康度 */
    private RedisHealthVO health;

    /** 当前实例资源概览 */
    private RedisStatsVO stats;

    /** Redis 命令统计列表 */
    private List<RedisCommandStatsVO> commandStats;
}
