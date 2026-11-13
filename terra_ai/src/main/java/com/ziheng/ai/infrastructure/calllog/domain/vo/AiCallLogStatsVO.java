package com.ziheng.ai.infrastructure.calllog.domain.vo;

import lombok.Data;

/**
 * AI调用日志全局统计
 */
@Data
public class AiCallLogStatsVO {

    /**
     * 总调用次数
     */
    private Long totalCalls;

    /**
     * Token总消耗
     */
    private Long totalTokens;

    /**
     * 成功调用次数
     */
    private Long successCalls;

    /**
     * 失败调用次数
     */
    private Long failCalls;
}
