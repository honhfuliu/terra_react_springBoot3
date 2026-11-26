package com.ziheng.ai.infrastructure.knowledge.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentTask;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentTaskVO;
import com.ziheng.ai.infrastructure.knowledge.enums.DocumentTaskStatus;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document_task(AI知识库文档处理任务表)】的数据库操作Service
* @createDate 2026-11-21 17:15:56
*/
public interface AiKnowledgeDocumentTaskService extends IService<AiKnowledgeDocumentTask> {
    /**
     * 创建文档解析任务
     *
     * @param documentId 文档ID
     * @return 任务记录
     */
    AiKnowledgeDocumentTask createParseTask(Long documentId);

    /**
     * 创建文档切分任务
     *
     * @param documentId 文档ID
     * @return 任务记录
     */
    AiKnowledgeDocumentTask createChunkTask(Long documentId);

    /**
     * 创建文档向量化任务
     *
     * @param documentId 文档ID
     * @return 任务记录
     */
    AiKnowledgeDocumentTask createEmbeddingTask(Long documentId);

    /**
     * 更新任务为处理中
     *
     * @param taskId         任务ID
     * @param externalTaskId Docling任务ID
     */
    void markProcessing(Long taskId, String externalTaskId);

    /**
     * 更新任务为失败
     *
     * @param taskId       任务ID
     * @param errorMessage 失败原因
     */
    DocumentTaskStatus markFailed(Long taskId, String errorMessage);


    /**
     * 删除文档对应的所有处理任务
     */
    void deleteByDocumentId(Long documentId);

    /**
     * 根据文档 ID 查询任务列表
     */
    List<AiKnowledgeDocumentTaskVO> listByDocumentId(Long documentId);

    /**
     * 验证文档处理任务是否完成
     * @param documentId 文档ID
     */
    void validateTaskFinished(Long documentId);
}
