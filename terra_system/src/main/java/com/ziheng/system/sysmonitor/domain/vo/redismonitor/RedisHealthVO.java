package com.ziheng.system.sysmonitor.domain.vo.redismonitor;

import lombok.Data;

import java.util.List;

/**
 * Redis 健康检查结果
 */
@Data
public class RedisHealthVO {
    /** 健康评分，例如 97 */
    private Integer score;

    /** 健康等级：GOOD / WARNING / CRITICAL */
    private String level;

    /** 健康状态描述 */
    private String message;

    /** 扣分原因列表 */
    private List<String> deductionReasons;
}
