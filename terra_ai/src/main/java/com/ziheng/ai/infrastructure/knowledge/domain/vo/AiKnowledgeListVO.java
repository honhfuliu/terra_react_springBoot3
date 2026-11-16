package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * AI知识库列表视图对象
 */
@Data
public class AiKnowledgeListVO {

    /**
     * 主键（操作列使用）
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
     * Embedding模型ID
     */
    private Long embeddingModelId;

    /**
     * Embedding模型名称
     */
    private String embeddingModelName;

    /**
     * 向量维度
     */
    private Integer embeddingDimension;

    /**
     * 切片长度
     */
    private Integer chunkSize;

    /**
     * 切片重叠长度
     */
    private Integer chunkOverlap;

    /**
     * Embedding模型所属提供商名称
     */
    private String providerName;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 切片数量
     */
    private Long chunkCount;


    /**
     * 状态（0停用 1启用）
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;
}
