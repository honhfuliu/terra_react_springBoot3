package com.ziheng.ai.capability.embedding.impl;

import com.ziheng.ai.capability.embedding.AiEmbeddingService;
import com.ziheng.ai.infrastructure.client.context.AiEmbeddingContext;
import com.ziheng.ai.infrastructure.client.factory.AiEmbeddingClientFactory;
import com.ziheng.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiEmbeddingServiceImpl implements AiEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(AiEmbeddingServiceImpl.class);

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

        EmbeddingModel embeddingModel = context.getEmbeddingModel();

        // 当前模型单次请求最多20条
        final int batchSize = 20;

        List<float[]> result = new ArrayList<>();

        // 分批调用模型
        for (int start = 0; start < texts.size(); start += batchSize) {

            int end = Math.min(start + batchSize, texts.size());

            List<String> batchTexts = texts.subList(start, end);

            log.info("Embedding 批次处理，当前范围：{}-{}，本批数量：{}", start, end - 1, batchTexts.size());

            List<float[]> batchVectors = embeddingModel.embed(batchTexts);

            if (batchVectors.size() != batchTexts.size()) {
                throw new BusinessException("Embedding 返回的向量数量与文本数量不一致");
            }

            result.addAll(batchVectors);
        }

        return result;
    }
}
