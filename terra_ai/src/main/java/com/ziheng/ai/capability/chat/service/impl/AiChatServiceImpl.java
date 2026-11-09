package com.ziheng.ai.capability.chat.service.impl;

import com.ziheng.ai.capability.chat.service.AiChatService;
import com.ziheng.ai.config.AiProperties;
import com.ziheng.ai.infrastructure.client.factory.AiChatClientFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final AiChatClientFactory aiChatClientFactory;

    private final AiProperties aiProperties;

    public AiChatServiceImpl(AiProperties aiProperties, AiChatClientFactory aiChatClientFactory) {
        this.aiProperties = aiProperties;
        this.aiChatClientFactory = aiChatClientFactory;
    }

    @Override
    public String chat(String message) {
        // 检查 AI 对话功能是否开启
        if (!aiProperties.getChat().isEnabled()) {
            throw new IllegalStateException("AI 对话功能已关闭");
        }
        ChatClient chatClient = aiChatClientFactory.create();
        // 调用底层 AI 客户端
        return chatClient.prompt().user(message).call().content();
    }
}
