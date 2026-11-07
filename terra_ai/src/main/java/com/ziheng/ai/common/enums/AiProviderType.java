package com.ziheng.ai.common.enums;

import lombok.Getter;

/**
 * AI 模型供应商类型
 */
@Getter
public enum AiProviderType {

    /**
     * 阿里云百炼
     */
    DASHSCOPE("dashscope", "阿里云百炼", true),

    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI", true),

    /**
     * DeepSeek
     */
    DEEPSEEK("deepseek", "DeepSeek", true),

    /**
     * Ollama 本地模型
     */
    OLLAMA("ollama", "Ollama", true);

    /**
     * 供应商编码
     */
    private final String code;

    /**
     * 供应商名称
     */
    private final String description;

    /**
     * 是否支持 OpenAI 兼容接口
     */
    private final boolean openAiCompatible;

    AiProviderType(String code, String description, boolean openAiCompatible) {
        this.code = code;
        this.description = description;
        this.openAiCompatible = openAiCompatible;
    }

    /**
     * 根据供应商编码获取枚举
     */
    public static AiProviderType getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (AiProviderType provider : values()) {
            if (provider.code.equalsIgnoreCase(code)) {
                return provider;
            }
        }

        return null;
    }
}