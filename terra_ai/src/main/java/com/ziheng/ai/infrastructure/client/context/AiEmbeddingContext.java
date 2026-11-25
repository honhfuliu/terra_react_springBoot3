package com.ziheng.ai.infrastructure.client.context;

import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.ai.embedding.EmbeddingModel;

/**
 * AI Embedding 上下文
 */
@Getter
@AllArgsConstructor
public class AiEmbeddingContext {

    /**
     * 动态 Embedding 模型
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 当前使用的模型
     */
    private final AiModel model;

    /**
     * 模型所属提供商
     */
    private final AiProvider provider;
}
