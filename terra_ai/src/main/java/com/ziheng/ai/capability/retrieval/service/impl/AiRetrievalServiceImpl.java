package com.ziheng.ai.capability.retrieval.service.impl;

import com.ziheng.ai.capability.embedding.AiEmbeddingService;
import com.ziheng.ai.capability.retrieval.service.AiRetrievalService;
import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.ziheng.ai.infrastructure.knowledge.mapper.AiKnowledgeMapper;
import com.ziheng.ai.infrastructure.vector.domain.dto.AiChunkSearchResult;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentEmbeddingService;
import com.ziheng.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiRetrievalServiceImpl implements AiRetrievalService {
    private final AiKnowledgeMapper aiKnowledgeMapper;
    private final AiDocumentEmbeddingService aiDocumentEmbeddingService;
    private final AiEmbeddingService aiEmbeddingService;

    public AiRetrievalServiceImpl(AiKnowledgeMapper aiKnowledgeMapper, AiDocumentEmbeddingService aiDocumentEmbeddingService,
                                  AiEmbeddingService aiEmbeddingService) {
        this.aiKnowledgeMapper = aiKnowledgeMapper;
        this.aiDocumentEmbeddingService = aiDocumentEmbeddingService;
        this.aiEmbeddingService = aiEmbeddingService;
    }

    @Override
    public List<AiChunkSearchResult> search(Long knowledgeId, String query, Double similarityThreshold, Integer topK) {
        AiKnowledge knowledge = aiKnowledgeMapper.selectById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException("知识库不存在");
        }
        // 校验 Embedding 配置
        if (knowledge.getEmbeddingModelId() == null || knowledge.getEmbeddingDimension() == null) {
            throw new BusinessException("知识库尚未配置 Embedding 模型");
        }
        float[] queryVector = aiEmbeddingService.embed(query, knowledge.getEmbeddingModelId(), knowledge.getEmbeddingDimension());
        if (queryVector == null || queryVector.length != knowledge.getEmbeddingDimension()) {
            throw new BusinessException("查询向量维度不正确");
        }
        return aiDocumentEmbeddingService.searchSimilar(knowledgeId, knowledge.getEmbeddingModelId(), queryVector, topK, similarityThreshold);
    }
}
