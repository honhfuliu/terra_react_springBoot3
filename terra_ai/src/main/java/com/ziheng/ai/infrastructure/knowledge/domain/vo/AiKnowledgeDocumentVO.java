package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * AI 知识库文档列表 VO
 */
@Data
public class AiKnowledgeDocumentVO {

    /**
     * 文档ID（操作列使用）
     */
    private Long id;

    /**
     * 文档名称
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * Chunk数量
     */
    private Integer chunkCount;

    /**
     * 解析状态
     */
    private String parseStatus;

    /**
     * 解析状态描述
     */
    private String parseStatusName;

    /**
     * 向量化状态
     */
    private String embeddingStatus;

    /**
     * 向量化状态描述
     */
    private String embeddingStatusName;

    /**
     * chunk状态
     */
    private String chunkStatus;
    /**
     * chunk状态描述
     */
    private String chunkStatusName;

    /**
     * 最近更新时间
     */
    private Date updateTime;
}
