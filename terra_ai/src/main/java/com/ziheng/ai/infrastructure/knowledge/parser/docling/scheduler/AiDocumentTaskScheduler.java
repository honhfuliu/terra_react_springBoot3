package com.ziheng.ai.infrastructure.knowledge.parser.docling.scheduler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocument;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentTask;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingChunk;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingParseResult;
import com.ziheng.ai.infrastructure.knowledge.enums.DocumentTaskStatus;
import com.ziheng.ai.infrastructure.knowledge.enums.DocumentTaskType;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.DoclingResultService;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.enums.DocumentFormat;
import com.ziheng.ai.infrastructure.knowledge.parser.docling.exception.DoclingTaskNotCompletedException;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentService;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentTaskService;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeService;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * AI 文档任务调度器
 */
@Component
public class AiDocumentTaskScheduler {
    private static final Logger log = LoggerFactory.getLogger(AiDocumentTaskScheduler.class);

    private final AiKnowledgeDocumentTaskService taskService;

    private final DoclingResultService doclingResultService;

    private final AiKnowledgeDocumentService documentService;

    private final AiKnowledgeService aiKnowledgeService;

    private final AiDocumentChunkService chunkService;


    public AiDocumentTaskScheduler(AiKnowledgeDocumentTaskService taskService, DoclingResultService doclingResultService,
                                   AiKnowledgeDocumentService documentService, AiKnowledgeService aiKnowledgeService,
                                   AiDocumentChunkService chunkService) {
        this.taskService = taskService;
        this.doclingResultService = doclingResultService;
        this.documentService = documentService;
        this.aiKnowledgeService = aiKnowledgeService;
        this.chunkService = chunkService;

    }

    /**
     * 定时获取解析任务ID
     */
    @Scheduled(fixedDelayString = "15000")
    public void fetchPendingParseTasks() {
        log.info("开始获取解析任务ID");
        // 查询待解析任务
        List<AiKnowledgeDocumentTask> tasks = taskService.list(
                Wrappers.<AiKnowledgeDocumentTask>lambdaQuery()
                        .eq(AiKnowledgeDocumentTask::getTaskType, DocumentTaskType.PARSE.getCode())
                        .eq(AiKnowledgeDocumentTask::getTaskStatus, DocumentTaskStatus.PENDING.getCode())
                        .isNull(AiKnowledgeDocumentTask::getExternalTaskId)
                        .orderByAsc(AiKnowledgeDocumentTask::getCreateTime)
                        .last("LIMIT 50")
        );
        if (tasks.isEmpty()) {
            log.info("没有待解析任务");
            return;
        }
        log.info("开始处理待解析任务ID，本次任务数量：{}", tasks.size());
        for (AiKnowledgeDocumentTask task : tasks) {
            submitParseTask(task);
        }
    }

    /**
     * 获取解析任务ID
     *
     * @param task
     */
    private void submitParseTask(AiKnowledgeDocumentTask task) {
        AiKnowledgeDocument document = documentService.getById(task.getDocumentId());
        if (document == null){
            log.error("文档不存在， documentId={}",  task.getDocumentId());
            taskService.markFailed(task.getId(), "文档不存在");
            return;
        }
        try {
            String taskId = doclingResultService.submitParseTask(document.getFilePath(), document.getFileName(), document.getId(), DocumentFormat.fromExtension(document.getFileExt()));
            taskService.markProcessing(task.getId(), taskId);
        } catch (IOException e) {
            log.error("提交解析任务失败，taskId={}, documentId={}, knowledgeId={}", task.getId(), task.getDocumentId(), document.getKnowledgeId(), e);
            taskService.markFailed(task.getId(), "提交解析任务失败");
            throw new RuntimeException(e);
        }
    }


    /**
     * 定时轮询 Docling 解析任务
     */
    @Scheduled(fixedDelayString = "15000")
    public void pollParseTasks() {
        log.info("开始轮询 Docling 解析任务");
        // 查询正在处理中的 PARSE 任务
        List<AiKnowledgeDocumentTask> tasks = taskService.list(
                Wrappers.<AiKnowledgeDocumentTask>lambdaQuery()
                        .eq(AiKnowledgeDocumentTask::getTaskType, DocumentTaskType.PARSE.getCode())
                        .eq(AiKnowledgeDocumentTask::getTaskStatus, DocumentTaskStatus.PROCESSING.getCode())
                        .isNotNull(AiKnowledgeDocumentTask::getExternalTaskId)
                        .orderByAsc(AiKnowledgeDocumentTask::getCreateTime)
                        .last("LIMIT 50")
        );
        if (tasks.isEmpty()) {
            log.info("没有正在处理中的解析任务");
            return;
        }
        log.info("开始处理 Docling 解析任务，本次任务数量：{}", tasks.size());
        for (AiKnowledgeDocumentTask task : tasks) {
            processParseTask(task);
        }
    }

    /**
     * 处理单个解析任务
     */
    public void processParseTask(AiKnowledgeDocumentTask task) {

        Long taskId = task.getId();
        Long documentId = task.getDocumentId();
        String externalTaskId = task.getExternalTaskId();
        try {
            log.info("开始检查 Docling 任务状态，taskId={}, documentId={}, externalTaskId={}", taskId, documentId, externalTaskId);

            DoclingParseResult result = doclingResultService.fetchAndSaveMarkdownAndJson(externalTaskId, documentId);

            // 事务更新文档表和任务表
            documentService.markParseSuccess(documentId, taskId, result);
            log.info("文档解析成功，taskId={}, documentId={}", taskId, documentId);
            // 创建切分任务
            AiKnowledgeDocumentTask chunkTask = taskService.createChunkTask(documentId);

        } catch (DoclingTaskNotCompletedException e) {
            // Docling 任务尚未完成，不更新本地状态，等待下次轮询
            log.debug("Docling 任务尚未完成，taskId={}, externalTaskId={}", taskId, externalTaskId);

        } catch (Exception e) {
            // 解析失败、结果获取失败或文件保存失败
            log.error("Docling 文档解析处理失败，taskId={}, documentId={}, externalTaskId={}", taskId, documentId, externalTaskId, e);

            // 事务更新文档表和任务表
            try {
                documentService.markParseFailed(documentId, taskId, e.getMessage());
            } catch (Exception updateException) {
                log.error("更新文档解析失败状态异常，taskId={}, documentId={}", taskId, documentId, updateException);
            }
        }
    }

    /**
     * 定时获取待切分任务ID
     */
    @Scheduled(fixedDelayString = "15000")
    public void fetchPendingChunkTasks() {
        log.info("开始获取待切分任务ID");
        // 查询 PENDING 的 CHUNK 任务，准备提交给 docling
        List<AiKnowledgeDocumentTask> tasks = taskService.list(
                Wrappers.<AiKnowledgeDocumentTask>lambdaQuery()
                        .eq(AiKnowledgeDocumentTask::getTaskType, DocumentTaskType.CHUNK.getCode())
                        .eq(AiKnowledgeDocumentTask::getTaskStatus, DocumentTaskStatus.PENDING.getCode())
                        .isNull(AiKnowledgeDocumentTask::getExternalTaskId)
                        .orderByAsc(AiKnowledgeDocumentTask::getCreateTime)
                        .last("LIMIT 50")
        );
        if (tasks.isEmpty()) {
            log.info("没有待切分任务");
            return;
        }
        log.info("开始处理待切分任务ID，本次任务数量：{}", tasks.size());
        for (AiKnowledgeDocumentTask task : tasks) {
            submitChunkTask(task);
        }
    }

    /**
     * 处理单个待切分任务：提交给 docling，获取 chunk 任务 ID 并回写
     */
    private void submitChunkTask(AiKnowledgeDocumentTask task) {
        AiKnowledgeDocument document = documentService.getById(task.getDocumentId());
        if (document == null){
            log.error("文档不存在， documentId={}",  task.getDocumentId());
            taskService.markFailed(task.getId(), "文档不存在");
            return;
        }
        AiKnowledge knowledge = aiKnowledgeService.getById(document.getKnowledgeId());
        if (knowledge == null){
            log.error("知识库不存在， documentId={}, knowledgeId={}",  task.getDocumentId(), document.getKnowledgeId());
            taskService.markFailed(task.getId(), "知识库不存在");
            return;
        }
        try {
            String taskId = doclingResultService.submitChunkTask(document.getParsedJsonPath(), knowledge.getChunkSize(), document.getId());
            // 更新chunk任务状态为处理中
            documentService.markChunkProcessing(document.getId(), task.getId(), taskId);
        } catch (Exception e) {
            log.error("提交解析任务失败，taskId={}, documentId={}, knowledgeId={}", task.getId(), task.getDocumentId(), document.getKnowledgeId(), e);
            taskService.markFailed(task.getId(), "提交切分任务失败");
            throw new RuntimeException(e);
        }
    }


    /**
     * 定时轮询 Docling 切分任务结果
     */
    @Scheduled(fixedDelayString = "15000")
    public void pollChunkTasks() {
        // 轮询 docling，获取 chunk 任务状态/结果
        List<AiKnowledgeDocumentTask> tasks = taskService.list(
                Wrappers.<AiKnowledgeDocumentTask>lambdaQuery()
                        .eq(AiKnowledgeDocumentTask::getTaskType, DocumentTaskType.CHUNK.getCode())
                        .eq(AiKnowledgeDocumentTask::getTaskStatus, DocumentTaskStatus.PROCESSING.getCode())
                        .isNotNull(AiKnowledgeDocumentTask::getExternalTaskId)
                        .orderByAsc(AiKnowledgeDocumentTask::getCreateTime)
                        .last("LIMIT 50")
        );
        if (tasks.isEmpty()) {
            log.info("没有正在处理中的切分任务");
            return;
        }
        log.info("开始处理 Docling 切分任务，本次任务数量：{}", tasks.size());
        for (AiKnowledgeDocumentTask task : tasks) {
            processChunkTask(task);
        }
    }

    /**
     * 处理单个切分任务
     */
    private void processChunkTask(AiKnowledgeDocumentTask task) {
        Long taskId = task.getId();
        Long documentId = task.getDocumentId();
        String externalTaskId = task.getExternalTaskId();
        try {
            log.info("开始获取 Docling 切片结果，taskId={}, documentId={}, externalTaskId={}", taskId, documentId, externalTaskId);

            // 1. 根据 externalTaskId 查询 Docling 切片结果
            List<DoclingChunk> result = doclingResultService.fetchChunkResult(externalTaskId);

            // 2. 保存 Chunk 到 PostgreSQL（先删除旧分块，保证重试幂等）
            AiKnowledgeDocument document = documentService.getById(documentId);
            if (document == null) {
                throw new IllegalStateException("文档不存在，documentId=" + documentId);
            }
            chunkService.saveChunks(document.getKnowledgeId(), documentId, result);

            // 3. 更新文档切片状态为 SUCCESS（同一事务内更新当前任务并创建 Embedding PENDING 任务）
            documentService.markChunkSuccess(documentId, taskId, result.size());
            log.info("文档切片成功，taskId={}, documentId={}, chunkCount={}", taskId, documentId, result.size());

        } catch (DoclingTaskNotCompletedException e) {
            // Docling 任务尚未完成，不更新本地状态，等待下次轮询
            log.debug("Docling 切片任务尚未完成，taskId={}, externalTaskId={}", taskId, externalTaskId);
        } catch (Exception e) {
            // 切片结果获取失败、入库失败或状态更新失败
            log.error("Docling 文档切片处理失败，taskId={}, documentId={}, externalTaskId={}", taskId, documentId, externalTaskId, e);
            try {
                documentService.markChunkFailed(documentId, taskId, e.getMessage());
            } catch (Exception updateException) {
                log.error("更新文档切片失败状态异常，taskId={}, documentId={}", taskId, documentId, updateException);
            }
        }
    }
}
