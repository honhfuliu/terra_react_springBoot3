package com.ziheng.ai.infrastructure.provider.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI模型提供商分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiProviderQuery extends PageQuery {

    /**
     * 关键字（模糊匹配提供商编码或提供商名称）
     */
    private String keyword;

    /**
     * 接入协议（精确匹配）
     */
    private String protocol;
}
