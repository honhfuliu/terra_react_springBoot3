package com.ziheng.ai.capability.embedding;

import com.ziheng.ai.infrastructure.client.context.AiEmbeddingContext;

import java.util.List;

public interface AiEmbeddingService {
    /**
     * 单条文本向量化
     */
    float[] embed(String text, Long modelId, Integer dimension);

    /**
     * 批量文本向量化
     */
    List<float[]> embed(List<String> texts, Long modelId, Integer dimension);
}
