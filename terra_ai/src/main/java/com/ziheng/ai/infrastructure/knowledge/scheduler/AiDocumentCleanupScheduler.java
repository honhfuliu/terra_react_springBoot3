package com.ziheng.ai.infrastructure.knowledge.scheduler;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentCleanupTask;
import com.ziheng.ai.infrastructure.knowledge.enums.AiDocumentCleanupType;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentCleanupTaskService;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库文档资源清理定时任务
 */
@Component
public class AiDocumentCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(AiDocumentCleanupScheduler.class);

    private final AiKnowledgeDocumentCleanupTaskService cleanupTaskService;
    private final FileService fileService;
    private final AiDocumentChunkService aiDocumentChunkService;

    public AiDocumentCleanupScheduler(AiKnowledgeDocumentCleanupTaskService cleanupTaskService, FileService fileService,
                                      AiDocumentChunkService aiDocumentChunkService) {
        this.cleanupTaskService = cleanupTaskService;
        this.fileService = fileService;
        this.aiDocumentChunkService = aiDocumentChunkService;
    }

    /**
     * 定时清理知识库文档资源
     */
    @Scheduled(fixedDelayString = "60000")
    public void executeCleanupTasks() {
        // 1. 领取待执行任务（SQL 已限定 PENDING/RETRYING，）
        List<AiKnowledgeDocumentCleanupTask> tasks = cleanupTaskService.claimPendingTasks(50);
        if (tasks.isEmpty()) {
            return;
        }
        log.info("开始执行文档资源清理任务，数量：{}", tasks.size());

        for (AiKnowledgeDocumentCleanupTask task : tasks) {
            try {
                // 2 & 3. 根据任务类型执行清理
                String taskType = task.getTaskType();
                if (AiDocumentCleanupType.CHUNK_EMBEDDING_DELETE.getCode().equals(taskType)) {
                    aiDocumentChunkService.deleteByDocumentId(task.getDocumentId());
                } else if (AiDocumentCleanupType.FILE_DELETE.getCode().equals(taskType)) {
                    fileService.delete(task.getOriginalFilePath());
                    fileService.delete(task.getMarkdownFilePath());
                    fileService.delete(task.getJsonFilePath());
                } else {
                    throw new BusinessException("未知的清理任务类型：" + taskType);
                }

                // 4. 更新任务状态
                cleanupTaskService.markSuccess(task.getId());
                log.info("文档资源清理成功，taskId={}, documentId={}, taskType={}",
                        task.getId(), task.getDocumentId(), taskType);
            } catch (Exception e) {
                log.error("文档资源清理失败，taskId={}, documentId={}, taskType={}",
                        task.getId(), task.getDocumentId(), task.getTaskType(), e);
                try {
                    cleanupTaskService.markFailed(task.getId(), e.getMessage());
                } catch (Exception updateException) {
                    log.error("更新清理任务失败状态异常，taskId={}", task.getId(), updateException);
                }
            }
        }
    }
}
