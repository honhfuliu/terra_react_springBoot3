package com.ziheng.ai.capability.rag.service;

import com.ziheng.ai.capability.rag.domain.dto.AiRagRequest;
import com.ziheng.ai.capability.rag.domain.vo.AiRagResponse;

public interface AiRagService {
    /**
     * RAG 问答
     *
     * @param request RAG 请求
     * @return RAG 回答
     */
    AiRagResponse chat(AiRagRequest request);
}
