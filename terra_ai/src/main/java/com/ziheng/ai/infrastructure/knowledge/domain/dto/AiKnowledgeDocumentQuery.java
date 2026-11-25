package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI知识库文档分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiKnowledgeDocumentQuery extends PageQuery {

    /**
     * 知识库ID（必填）
     */
    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeId;

    /**
     * 文档名称（模糊匹配）
     */
    private String fileName;
}
