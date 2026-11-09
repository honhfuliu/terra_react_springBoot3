package com.ziheng.ai.infrastructure.model.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI模型分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiModelQuery extends PageQuery {

    /**
     * 关键字（模糊匹配模型名称或模型编码）
     */
    private String keyword;

    /**
     * 模型类型（精确匹配）
     */
    private String modelType;

    /**
     * 状态 0停用 1启用（精确匹配）
     */
    private String enabled;
}
