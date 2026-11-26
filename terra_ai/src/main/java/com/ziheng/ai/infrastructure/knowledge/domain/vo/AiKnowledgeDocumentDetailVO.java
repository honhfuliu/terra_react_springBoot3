package com.ziheng.ai.infrastructure.knowledge.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * AI 知识库文档详情视图对象
 */
@Data
public class AiKnowledgeDocumentDetailVO {

    /**
     * 文档ID
     */
    private Long id;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 解析状态
     */
    private String parseStatus;

    /**
     * 解析状态描述
     */
    private String parseStatusName;

    /**
     * 文件类型（扩展名）
     */
    private String fileExt;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 所属知识库ID
     */
    private Long knowledgeId;

    /**
     * 所属知识库名称
     */
    private String knowledgeName;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 文档页数
     */
    private Integer pageCount;

    /**
     * Chunk数量
     */
    private Integer chunkCount;

    /**
     * 向量化状态
     */
    private String embeddingStatus;

    /**
     * 向量化状态描述
     */
    private String embeddingStatusName;

    /**
     * 所属知识库的向量模型ID
     */
    private Long embeddingModelId;

    /**
     * 所属知识库的向量模型名称
     */
    private String embeddingModelName;

    /**
     * 切分状态
     */
    private String chunkStatus;
    /**
     * 切分状态描述
     */
    private String chunkStatusName;

    private List<AiKnowledgeDocumentTaskVO> tasks;
}
