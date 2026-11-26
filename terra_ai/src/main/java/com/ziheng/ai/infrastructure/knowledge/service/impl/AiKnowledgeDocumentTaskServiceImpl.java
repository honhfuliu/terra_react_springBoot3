package com.ziheng.ai.infrastructure.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentTask;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentTaskVO;
import com.ziheng.ai.infrastructure.knowledge.enums.DocumentTaskStatus;
import com.ziheng.ai.infrastructure.knowledge.enums.DocumentTaskType;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentTaskService;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeDocumentTaskMapper;
import com.ziheng.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document_task(AI知识库文档处理任务表)】的数据库操作Service实现
* @createDate 2026-11-21 17:15:56
*/
@Service
public class AiKnowledgeDocumentTaskServiceImpl extends ServiceImpl<AiKnowledgeDocumentTaskMapper, AiKnowledgeDocumentTask>
    implements AiKnowledgeDocumentTaskService{

    /**
     * 创建文档解析任务
     * @param documentId 文档ID
     * @return 任务记录
     */
    @Override
    public AiKnowledgeDocumentTask createParseTask(Long documentId) {
        AiKnowledgeDocumentTask task = new AiKnowledgeDocumentTask();

        task.setDocumentId(documentId);
        task.setTaskType(DocumentTaskType.PARSE.getCode());
        task.setTaskStatus(DocumentTaskStatus.PENDING.getCode());
        task.setRetryCount(0);
        task.setMaxRetryCount(3);

        boolean success = this.save(task);

        if (!success) {
            throw new BusinessException("创建文档解析任务失败");
        }

        return task;
    }
    /**
     * 创建文档切分任务
     * @param documentId 文档ID
     * @return 任务记录
     */
    @Override
    public AiKnowledgeDocumentTask createChunkTask(Long documentId) {
        AiKnowledgeDocumentTask task = new AiKnowledgeDocumentTask();

        task.setDocumentId(documentId);
        task.setTaskType(DocumentTaskType.CHUNK.getCode());
        task.setTaskStatus(DocumentTaskStatus.PENDING.getCode());
        task.setRetryCount(0);
        task.setMaxRetryCount(3);

        boolean success = this.save(task);

        if (!success) {
            throw new BusinessException("创建文档解析任务失败");
        }
        return task;
    }

    /**
     * 创建文档向量化任务
     * @param documentId 文档ID
     * @return 任务记录
     */
    @Override
    public AiKnowledgeDocumentTask createEmbeddingTask(Long documentId) {
        AiKnowledgeDocumentTask task = new AiKnowledgeDocumentTask();

        task.setDocumentId(documentId);
        task.setTaskType(DocumentTaskType.EMBEDDING.getCode());
        task.setTaskStatus(DocumentTaskStatus.PROCESSING.getCode());
        task.setStartTime(new Date());
        task.setRetryCount(0);
        task.setMaxRetryCount(3);

        boolean success = this.save(task);

        if (!success) {
            throw new BusinessException("创建文档向量化任务失败");
        }
        return task;
    }

    /**
     * 标记任务为处理中
     * @param taskId 任务ID
     * @param externalTaskId 外部任务ID
     */
    @Override
    public void markProcessing(Long taskId, String externalTaskId) {
        AiKnowledgeDocumentTask task = this.getById(taskId);

        if (task == null) {
            throw new BusinessException("文档处理任务不存在");
        }

        task.setExternalTaskId(externalTaskId);
        task.setTaskStatus(DocumentTaskStatus.PROCESSING.getCode());
        task.setStartTime(new Date());

        if (!this.updateById(task)) {
            throw new BusinessException("更新文档处理任务失败");
        }
    }

    /**
     * 标记任务为失败
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    @Override
    public DocumentTaskStatus  markFailed(Long taskId, String errorMessage) {
        AiKnowledgeDocumentTask task = this.getById(taskId);
        if (task == null) {
            throw new BusinessException("文档处理任务不存在");
        }
        // 当前重试次数
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();

        // 最大重试次数
        int maxRetryCount = task.getMaxRetryCount() == null ? 0 : task.getMaxRetryCount();

        int nextRetryCount = retryCount + 1;

        // 判断是否耗尽重试次数
        boolean exhausted = nextRetryCount >= maxRetryCount;

        task.setRetryCount(nextRetryCount);
        task.setErrorMessage(errorMessage);
        DocumentTaskStatus status;
        if (exhausted) {
            status = DocumentTaskStatus.FAILED;
            task.setEndTime(new Date());
        } else {
            status = DocumentTaskStatus.RETRYING;
            task.setEndTime(null);
        }
        task.setTaskStatus(status.getCode());
        if (!this.updateById(task)) {
            throw new BusinessException("更新文档处理任务失败");
        }

        return status;
    }

    @Override
    public void deleteByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        baseMapper.delete(new LambdaQueryWrapper<AiKnowledgeDocumentTask>()
                .eq(AiKnowledgeDocumentTask::getDocumentId, documentId));
    }

    /**
     * 根据文档ID查询任务列表
      * @param documentId 文档ID
      * @return 任务列表
     */
    @Override
    public List<AiKnowledgeDocumentTaskVO> listByDocumentId(Long documentId) {
        LambdaQueryWrapper<AiKnowledgeDocumentTask> aiKnowledgeDocumentTaskLambdaQueryWrapper = new LambdaQueryWrapper<AiKnowledgeDocumentTask>()
                .eq(AiKnowledgeDocumentTask::getDocumentId, documentId)
                .orderByAsc(AiKnowledgeDocumentTask::getCreateTime);
        List<AiKnowledgeDocumentTask> documentTasks = baseMapper.selectList(aiKnowledgeDocumentTaskLambdaQueryWrapper);
        if (documentTasks.isEmpty()) {
            return List.of();
        }
        return AiKnowledgeDocumentTaskVO.from(documentTasks);
    }
}




