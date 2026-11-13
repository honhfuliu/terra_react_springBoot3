package com.ziheng.ai.infrastructure.calllog.domain.vo;

import lombok.Data;

/**
 * AI模型Token消耗统计
 */
@Data
public class AiModelConsumptionVO {

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型供应商名称
     */
    private String providerName;

    /**
     * 总Token
     */
    private Long totalTokens;

    /**
     * 调用次数
     */
    private Long callCount;

    /**
     * 输入Token
     */
    private Long promptTokens;

    /**
     * 输出Token
     */
    private Long completionTokens;
}
