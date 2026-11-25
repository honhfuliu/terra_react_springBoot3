package com.ziheng.ai.infrastructure.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocument;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentCleanupTask;
import com.ziheng.ai.infrastructure.knowledge.enums.AiDocumentCleanupType;
import com.ziheng.ai.infrastructure.knowledge.enums.AiDocumentProcessStatus;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentCleanupTaskService;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeDocumentCleanupTaskMapper;
import com.ziheng.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document_cleanup_task(知识库文档资源清理任务表)】的数据库操作Service实现
* @createDate 2026-11-25 14:47:54
*/
@Service
public class AiKnowledgeDocumentCleanupTaskServiceImpl extends ServiceImpl<AiKnowledgeDocumentCleanupTaskMapper, AiKnowledgeDocumentCleanupTask>
    implements AiKnowledgeDocumentCleanupTaskService{

    private static final int DEFAULT_BATCH_SIZE = 50;

    /**
     * 创建文件删除任务
     * @param document 文档信息
     */
    @Override
    public void createFileDeleteTask(AiKnowledgeDocument document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("文档信息不能为空");
        }
        AiKnowledgeDocumentCleanupTask task = new AiKnowledgeDocumentCleanupTask();

        task.setKnowledgeId(document.getKnowledgeId());
        task.setDocumentId(document.getId());
        task.setTaskType(AiDocumentCleanupType.FILE_DELETE.getCode());

        task.setOriginalFilePath(document.getFilePath());
        task.setMarkdownFilePath(document.getParsedMdPath());
        task.setJsonFilePath(document.getParsedJsonPath());

        task.setStatus(AiDocumentProcessStatus.PENDING.getCode());
        task.setRetryCount(0);
        task.setMaxRetryCount(3);

        save(task);
    }

    /**
     * 创建 Chunk 和 Embedding 删除任务
     * @param knowledgeId 知识库ID
     * @param documentId 文档ID
     */
    @Override
    public void createChunkEmbeddingDeleteTask(Long knowledgeId, Long documentId) {
        if (knowledgeId == null || documentId == null) {
            throw new IllegalArgumentException("知识库ID或文档ID不能为空");
        }
        AiKnowledgeDocumentCleanupTask task = new AiKnowledgeDocumentCleanupTask();

        task.setKnowledgeId(knowledgeId);
        task.setDocumentId(documentId);
        task.setTaskType(AiDocumentCleanupType.CHUNK_EMBEDDING_DELETE.getCode());

        task.setStatus(AiDocumentProcessStatus.PENDING.getCode());
        task.setRetryCount(0);
        task.setMaxRetryCount(3);

        save(task);
    }
    /**
     * 领取待执行清理任务
     * @param limit 最大数量
     * @return 任务列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AiKnowledgeDocumentCleanupTask> claimPendingTasks(int limit) {
        if (limit <= 0) {
            limit = DEFAULT_BATCH_SIZE;
        }

        // 1. 查询并锁定待执行任务
        List<AiKnowledgeDocumentCleanupTask> tasks = baseMapper.selectPendingTasksForUpdate(limit);
        if (tasks.isEmpty()) {
            return tasks;
        }

        // 2. 更新任务状态为 PROCESSING，确保领取生效且不会被重复领取
        Date now = new Date();
        for (AiKnowledgeDocumentCleanupTask task : tasks) {
            task.setStatus(AiDocumentProcessStatus.PROCESSING.getCode());
            task.setProcessStartTime(now);
            task.setProcessEndTime(null);
            task.setErrorMessage(null);

            if (!updateById(task)) {
                throw new BusinessException("领取清理任务失败");
            }
        }

        return tasks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSuccess(Long taskId) {
        if (taskId == null) {
            throw new BusinessException("清理任务ID不能为空");
        }

        boolean success = update(new LambdaUpdateWrapper<AiKnowledgeDocumentCleanupTask>()
                        .eq(AiKnowledgeDocumentCleanupTask::getId, taskId)
                        .eq(AiKnowledgeDocumentCleanupTask::getStatus, AiDocumentProcessStatus.PROCESSING.getCode())
                        .set(AiKnowledgeDocumentCleanupTask::getStatus, AiDocumentProcessStatus.SUCCESS.getCode())
                        .set(AiKnowledgeDocumentCleanupTask::getProcessEndTime, new Date())
                        .set(AiKnowledgeDocumentCleanupTask::getErrorMessage, null));

        if (!success) {
            throw new BusinessException("清理任务状态更新失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markFailed(Long taskId, String errorMessage) {
        if (taskId == null) {
            throw new BusinessException("清理任务ID不能为空");
        }

        AiKnowledgeDocumentCleanupTask task = getById(taskId);

        if (task == null) {
            throw new BusinessException("清理任务不存在");
        }

        if (!AiDocumentProcessStatus.PROCESSING.getCode()
                .equals(task.getStatus())) {
            throw new BusinessException("当前清理任务状态不允许标记失败");
        }

        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();

        int maxRetryCount = task.getMaxRetryCount() == null ? 3 : task.getMaxRetryCount();

        retryCount++;

        String status = retryCount >= maxRetryCount ? AiDocumentProcessStatus.FAILED.getCode() : AiDocumentProcessStatus.RETRYING.getCode();

        boolean success = update(
                new LambdaUpdateWrapper<AiKnowledgeDocumentCleanupTask>()
                        .eq(AiKnowledgeDocumentCleanupTask::getId, taskId)
                        .eq(AiKnowledgeDocumentCleanupTask::getStatus, AiDocumentProcessStatus.PROCESSING.getCode())
                        .set(AiKnowledgeDocumentCleanupTask::getRetryCount, retryCount)
                        .set(AiKnowledgeDocumentCleanupTask::getStatus, status)
                        .set(AiKnowledgeDocumentCleanupTask::getErrorMessage, truncateErrorMessage(errorMessage))
                        .set(AiKnowledgeDocumentCleanupTask::getProcessEndTime, new Date()));

        if (!success) {
            throw new BusinessException("清理任务失败状态更新失败");
        }
    }
    /**
     * 避免异常信息超过数据库字段长度
     */
    private String truncateErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "清理任务执行失败";
        }
        int maxLength = 2000;
        return errorMessage.length() > maxLength ? errorMessage.substring(0, maxLength) : errorMessage;
    }

}




