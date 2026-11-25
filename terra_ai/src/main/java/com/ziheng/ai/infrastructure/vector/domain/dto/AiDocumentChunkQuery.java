package com.ziheng.ai.infrastructure.vector.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI知识库文档分块分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDocumentChunkQuery extends PageQuery {

    /**
     * 文档ID（必填）
     */
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    /**
     * 分块内容（模糊匹配）
     */
    private String content;
}
