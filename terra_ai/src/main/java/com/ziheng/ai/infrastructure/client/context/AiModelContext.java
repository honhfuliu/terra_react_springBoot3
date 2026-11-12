package com.ziheng.ai.infrastructure.client.context;

import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模型上下文
 */
@Getter
@AllArgsConstructor
public class AiModelContext {
    /**
     * 当前使用的模型
     */
    private final AiModel model;

    /**
     * 模型所属提供商
     */
    private final AiProvider provider;
}
