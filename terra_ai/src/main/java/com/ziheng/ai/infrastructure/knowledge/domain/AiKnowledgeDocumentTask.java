package com.ziheng.ai.infrastructure.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI知识库文档处理任务表
 * @TableName ai_knowledge_document_task
 */
@TableName(value ="ai_knowledge_document_task")
@Data
public class AiKnowledgeDocumentTask {
    /**
     * 任务记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属文档ID
     */
    private Long documentId;

    /**
     * 任务类型：PARSE解析、CHUNK切片、EMBEDDING向量化
     */
    private String taskType;

    /**
     * 任务状态：PENDING待处理、RUNNING处理中、SUCCESS成功、FAILED失败、RETRYING待重试
     */
    private String taskStatus;

    /**
     * 外部任务ID，例如Docling task_id
     */
    private String externalTaskId;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 任务失败原因
     */
    private String errorMessage;

    /**
     * 任务开始时间
     */
    private Date startTime;

    /**
     * 任务完成时间
     */
    private Date endTime;

    /**
     * 下次重试时间
     */
    private Date nextRetryTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}