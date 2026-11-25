package com.ziheng.ai.infrastructure.knowledge.service;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocument;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentCleanupTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document_cleanup_task(知识库文档资源清理任务表)】的数据库操作Service
* @createDate 2026-11-25 14:47:54
*/
public interface AiKnowledgeDocumentCleanupTaskService extends IService<AiKnowledgeDocumentCleanupTask> {
    /**
     * 创建文件删除任务
     */
    void createFileDeleteTask(AiKnowledgeDocument document);

    /**
     * 创建 Chunk 和 Embedding 删除任务
     */
    void createChunkEmbeddingDeleteTask(Long knowledgeId, Long documentId);

    /**
     * 领取待执行清理任务
     */
    List<AiKnowledgeDocumentCleanupTask> claimPendingTasks(int limit);

    /**
     * 标记任务执行成功
     */
    void markSuccess(Long taskId);

    /**
     * 标记任务执行失败
     */
    void markFailed(Long taskId, String errorMessage);


}
