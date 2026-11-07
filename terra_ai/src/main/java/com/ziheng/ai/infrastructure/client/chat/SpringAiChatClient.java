package com.ziheng.ai.infrastructure.client.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * Spring AI 对话客户端实现
 *
 * 使用 Spring AI ChatClient 实现统一的 AI 对话调用接口。
 */
@Component
public class SpringAiChatClient implements AiChatClient {
    // Spring AI 对话客户端
    private final ChatClient chatClient;

    public SpringAiChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    /**
     * 普通文本对话
     *
     * @param message 用户输入内容
     * @return AI 模型生成的回答
     */
    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
