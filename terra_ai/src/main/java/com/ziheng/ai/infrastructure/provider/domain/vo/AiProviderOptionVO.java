package com.ziheng.ai.infrastructure.provider.domain.vo;

import lombok.Data;

/**
 * AI模型提供商下拉选项
 */
@Data
public class AiProviderOptionVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 提供商名称
     */
    private String providerName;
}
