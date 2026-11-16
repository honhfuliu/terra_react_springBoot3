package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI知识库分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiKnowledgeQuery extends PageQuery {

    /**
     * 关键字（模糊匹配知识库名称或知识库描述）
     */
    private String keyword;
}
