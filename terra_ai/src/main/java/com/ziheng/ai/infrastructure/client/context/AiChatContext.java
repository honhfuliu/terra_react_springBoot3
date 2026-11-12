package com.ziheng.ai.infrastructure.client.context;

import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;

/**
 * AI 对话上下文
 */
@Getter
@AllArgsConstructor
public class AiChatContext {
    /**
     * 动态聊天客户端
     */
    private final ChatClient chatClient;

    /**
     * 当前使用的模型
     */
    private final AiModel model;

    /**
     * 模型所属提供商
     */
    private final AiProvider provider;
}
