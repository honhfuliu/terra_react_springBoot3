package com.ziheng.ai.infrastructure.model.domain.vo;

import lombok.Data;

/**
 * AI模型选项（下拉/列表使用）
 */
@Data
public class AiModelOptionVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 模型编码
     */
    private String modelCode;
}
