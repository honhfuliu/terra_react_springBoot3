package com.ziheng.ai.infrastructure.model.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * AI向量模型选项（下拉/列表使用）
 */
@Data
public class AiVectorModelVO {

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 供应商名称
     */
    private String providerName;

    /**
     * 默认Embedding维度
     */
    private Integer defaultEmbeddingDimension;

    /**
     * 支持的Embedding维度
     */
    private List<Integer> supportedDimensions;
}
