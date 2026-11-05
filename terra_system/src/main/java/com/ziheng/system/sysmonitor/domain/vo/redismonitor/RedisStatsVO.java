package com.ziheng.system.sysmonitor.domain.vo.redismonitor;

import lombok.Data;

/**
 * Redis 统计信息
 */
@Data
public class RedisStatsVO {
    /** 当前键数量 */
    private Long keyCount;

    /** 累计过期键数量 */
    private Long expiredKeys;

    /** 阻塞客户端数量 */
    private Long blockedClients;

    /** 内存淘汰策略 */
    private String evictionPolicy;
}
