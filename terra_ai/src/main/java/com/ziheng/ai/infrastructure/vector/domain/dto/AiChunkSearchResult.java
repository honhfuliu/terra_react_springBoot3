package com.ziheng.ai.infrastructure.vector.domain.dto;
import lombok.Data;

import java.util.Map;

/**
 * 向量检索结果
 */
@Data
public class AiChunkSearchResult {

    /**
     * 文本块 ID
     */
    private Long chunkId;

    /**
     * 所属文档 ID
     */
    private Long documentId;

    /**
     * 文本块顺序
     */
    private Integer chunkIndex;

    /**
     * 文本块内容
     */
    private String content;

    /**
     * 文本块元数据
     */
    private Map<String, Object> metadata;

    /**
     * 余弦距离，越小越相似
     */
    private Double distance;
}