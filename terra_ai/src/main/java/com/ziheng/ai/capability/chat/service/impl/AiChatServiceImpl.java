package com.ziheng.ai.capability.chat.service.impl;

import com.ziheng.ai.capability.chat.service.AiChatService;
import com.ziheng.ai.config.AiProperties;
import com.ziheng.ai.infrastructure.client.chat.AiChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final AiChatClient aiChatClient;

    private final AiProperties aiProperties;

    public AiChatServiceImpl(AiChatClient aiChatClient, AiProperties aiProperties) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
    }

    @Override
    public String chat(String message) {
        // 检查 AI 对话功能是否开启
        if (!aiProperties.getChat().isEnabled()) {
            throw new IllegalStateException("AI 对话功能已关闭");
        }
        // 调用底层 AI 客户端
        return aiChatClient.chat(message);
    }
}
