package com.ziheng.system.sysmonitor.domain.vo.redismonitor;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Redis 实例信息
 */
@Data
public class RedisInstanceVO {

    /** 实例名称 */
    private String instanceName;

    /** 连接状态：UP / DOWN */
    private String status;

    /** 最近检测时间 */
    private LocalDateTime lastCheckTime;

    /** Redis 版本 */
    private String version;

    /** 运行时长（秒） */
    private Long uptimeSeconds;

    /** 连接地址 */
    private String address;

    /** 内存使用率（%） */
    private BigDecimal memoryUsagePercent;

    /** 已使用内存（字节） */
    private Long usedMemoryBytes;

    /** 最大内存限制（字节） */
    private Long maxMemoryBytes;

    /** 缓存命中率（%） */
    private BigDecimal hitRate;

    /** 当前连接数 */
    private Long connectedClients;

    /** 历史峰值连接数1 */
    private Long peakConnections;

    /** 当前每秒请求数1 */
    private BigDecimal qps;

    /** 相比上一分钟的 QPS 变化率（%）1 */
    private BigDecimal qpsChangePercent;
}
