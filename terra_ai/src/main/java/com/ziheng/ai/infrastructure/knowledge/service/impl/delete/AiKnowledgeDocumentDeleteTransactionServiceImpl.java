package com.ziheng.ai.infrastructure.knowledge.service.impl.delete;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocument;
import com.ziheng.ai.infrastructure.knowledge.enums.AiDocumentProcessStatus;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeDocumentMapper;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeMapper;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentCleanupTaskService;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentTaskService;
import com.ziheng.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识库文档删除事务服务
 */
@Service
@RequiredArgsConstructor
public class AiKnowledgeDocumentDeleteTransactionServiceImpl {
    private final AiKnowledgeMapper knowledgeMapper;

    private final AiKnowledgeDocumentMapper documentMapper;

    private final AiKnowledgeDocumentCleanupTaskService cleanupTaskService;

    private final AiKnowledgeDocumentTaskService documentTaskService;

    @Transactional(rollbackFor = Exception.class)
    public void deleteDocumentTransaction(Long documentId) {
        // 1. SELECT FOR UPDATE 查询并锁定文档
        LambdaQueryWrapper<AiKnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDocument::getId, documentId);
        wrapper.last("FOR UPDATE");
        AiKnowledgeDocument document = documentMapper.selectOne(wrapper);

        if (document == null) {
            throw new IllegalArgumentException("文档不存在");
        }

        // 2. 校验 PARSE、CHUNK、EMBEDDING 状态
        validateDocumentDelete(document);

        // 3. 更新知识库 documentCount、chunkCount
        knowledgeMapper.decrementDocumentCount(document.getKnowledgeId());
        knowledgeMapper.decrementChunkCount(document.getKnowledgeId(), document.getChunkCount());

        // 4. 创建 FILE_DELETE 任务
        cleanupTaskService.createFileDeleteTask(document);
        // 5. 创建 CHUNK_EMBEDDING_DELETE 任务
        cleanupTaskService.createChunkEmbeddingDeleteTask(document.getKnowledgeId(), document.getId());

        // 6. 删除普通文档处理任务
        documentTaskService.deleteByDocumentId(documentId);

        // 7. 删除文档记录
        int rows = documentMapper.deleteById(documentId);

        if (rows != 1) {
            throw new IllegalStateException("文档删除失败");
        }
    }


    private void validateDocumentDelete(AiKnowledgeDocument document) {

        validateProcessStatus("文档解析", document.getParseStatus());

        validateProcessStatus("文档切分", document.getChunkStatus());

        validateProcessStatus("文档向量化", document.getEmbeddingStatus());
    }

    /**
     * 校验处理阶段是否已经结束
     */
    private void validateProcessStatus(String stageName, String status) {

        if (!AiDocumentProcessStatus.SUCCESS.name().equals(status)
                && !AiDocumentProcessStatus.PENDING.name().equals(status)
                && !AiDocumentProcessStatus.FAILED.name().equals(status)) {

            throw new BusinessException(stageName + "尚未完成，暂时不能删除文档");
        }
    }

}
