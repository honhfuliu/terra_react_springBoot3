package com.ziheng.ai.capability.retrieval.service;

import com.ziheng.ai.infrastructure.vector.domain.dto.AiChunkSearchResult;

import java.util.List;

public interface AiRetrievalService {

    /**
     * 根据用户问题检索知识库相关内容
     *
     * @param knowledgeId        知识库ID
     * @param query              用户问题
     * @param similarityThreshold 相似度阈值
     * @param topK               返回的文档块数量
     * @return 相似文档块列表
     */
    List<AiChunkSearchResult> search(Long knowledgeId, String query, Double similarityThreshold, Integer topK);
}
