package com.ziheng.ai.capability.rag.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRagSource {

    /**
     * Chunk ID
     */
    private Long chunkId;

    /**
     * 文档 ID
     */
    private Long documentId;

    /**
     * Chunk 索引
     */
    private Integer chunkIndex;

    /**
     * 原始内容
     */
    private String content;

    /**
     * 相似度
     */
    private Double similarity;
}
