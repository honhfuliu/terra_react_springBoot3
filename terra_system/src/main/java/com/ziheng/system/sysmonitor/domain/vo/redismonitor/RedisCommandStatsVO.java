package com.ziheng.system.sysmonitor.domain.vo.redismonitor;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Redis 命令统计信息
 */
@Data
public class RedisCommandStatsVO {
    /** 命令名称，例如 GET、SET、HGET、其他命令 */
    private String commandName;

    /** 调用次数 */
    private Long calls;

    /** 调用占比（%） */
    private BigDecimal percentage;

    /** 平均耗时（毫秒） */
    private BigDecimal avgTimeMs;
}
