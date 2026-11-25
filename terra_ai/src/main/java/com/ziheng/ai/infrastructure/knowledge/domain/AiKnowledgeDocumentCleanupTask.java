package com.ziheng.ai.infrastructure.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 知识库文档资源清理任务表
 * @TableName ai_knowledge_document_cleanup_task
 */
@TableName(value ="ai_knowledge_document_cleanup_task")
@Data
public class AiKnowledgeDocumentCleanupTask {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeId;

    /**
     * 文档ID
     */
    private Long documentId;

    /**
     * 清理任务类型：FILE_DELETE、CHUNK_EMBEDDING_DELETE
     */
    private String taskType;

    /**
     * 原始文件路径
     */
    private String originalFilePath;

    /**
     * Markdown解析文件路径
     */
    private String markdownFilePath;

    /**
     * Docling JSON文件路径
     */
    private String jsonFilePath;

    /**
     * 任务状态：PENDING、PROCESSING、RETRYING、SUCCESS、FAILED
     */
    private String status;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 最近一次错误信息
     */
    private String errorMessage;

    /**
     * 最近一次处理开始时间
     */
    private Date processStartTime;

    /**
     * 处理结束时间
     */
    private Date processEndTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}