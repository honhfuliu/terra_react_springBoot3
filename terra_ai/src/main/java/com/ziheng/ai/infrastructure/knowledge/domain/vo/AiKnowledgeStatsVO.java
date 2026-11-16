package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import lombok.Data;

/**
 * AI知识库统计信息
 */
@Data
public class AiKnowledgeStatsVO {

    /**
     * 知识库总数
     */
    private Long knowledgeCount;

    /**
     * 文档总数
     */
    private Long documentCount;

    /**
     * Chunk总数
     */
    private Long chunkCount;
}
