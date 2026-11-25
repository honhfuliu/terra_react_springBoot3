package com.ziheng.ai.capability.embedding.impl;

import com.ziheng.ai.capability.embedding.AiEmbeddingService;
import com.ziheng.ai.infrastructure.client.context.AiEmbeddingContext;
import com.ziheng.ai.infrastructure.client.factory.AiEmbeddingClientFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiEmbeddingServiceImpl implements AiEmbeddingService {
    private final AiEmbeddingClientFactory aiEmbeddingClientFactory;

    public AiEmbeddingServiceImpl(AiEmbeddingClientFactory aiEmbeddingClientFactory) {
        this.aiEmbeddingClientFactory = aiEmbeddingClientFactory;
    }

    /**
     * 单条文本向量化
     * @param text 文本内容
     * @param modelId 模型ID 不传递使用默认模型
     * @param dimension 向量维度 不传递使用默认维度
     * @return 向量化后的向量
     */
    @Override
    public float[] embed(String text, Long modelId, Integer dimension) {
        // 创建 Embedding 上下文
        AiEmbeddingContext context = aiEmbeddingClientFactory.create(modelId, dimension);

        // 调用模型生成向量
        return context.getEmbeddingModel().embed(text);
    }

    /**
     * 批量文本向量化
     * @param texts 文本内容列表
     * @param modelId 模型ID 不传递使用默认模型
     * @param dimension 向量维度 不传递使用默认维度
     * @return 向量化后的向量列表
     */
    @Override
    public List<float[]> embed(List<String> texts, Long modelId, Integer dimension) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        // 创建 Embedding 上下文
        AiEmbeddingContext context = aiEmbeddingClientFactory.create(modelId, dimension);

        // 批量调用模型生成向量
        return context.getEmbeddingModel().embed(texts);
    }
}
