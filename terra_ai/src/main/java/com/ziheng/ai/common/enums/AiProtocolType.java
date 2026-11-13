package com.ziheng.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI协议类型
 */
@Getter
@AllArgsConstructor
public enum AiProtocolType {

    OPENAI_COMPATIBLE("OPENAI_COMPATIBLE", "OpenAI 兼容协议"),
    OLLAMA("OLLAMA", "Ollama 原生协议");

    private final String code;

    private final String description;
}