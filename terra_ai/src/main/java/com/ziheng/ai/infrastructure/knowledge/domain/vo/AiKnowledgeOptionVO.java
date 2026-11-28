package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 知识库选项VO（用于前端选择框，仅含知识库ID与名称）
 */
@Data
public class AiKnowledgeOptionVO implements Serializable {

    /**
     * 知识库ID
     */
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * Chunk 数量
     */
    private Long chunkCount;

    /**
     * Embedding 模型编码
     */
    private String embeddingModelCode;

    /**
     * 状态（0停用 1启用）
     */
    private String status;
}
