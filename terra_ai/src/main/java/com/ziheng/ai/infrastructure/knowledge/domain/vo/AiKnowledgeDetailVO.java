package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * AI知识库详情视图对象
 */
@Data
public class AiKnowledgeDetailVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 文档总数
     */
    private Integer documentCount;

    /**
     * Chunk总数
     */
    private Long chunkCount;

    /**
     * Embedding模型ID
     */
    private Long embeddingModelId;

    /**
     * Embedding模型名称
     */
    private String embeddingModelName;

    /**
     * Embedding模型维度
     */
    private Integer embeddingDimension;

    /**
     * Embedding模型所属供应商名称
     */
    private String providerName;

    /**
     * 切片长度
     */
    private Integer chunkSize;

    /**
     * 切片重叠长度
     */
    private Integer chunkOverlap;

    /**
     * 状态 0停用 1启用
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;
}
