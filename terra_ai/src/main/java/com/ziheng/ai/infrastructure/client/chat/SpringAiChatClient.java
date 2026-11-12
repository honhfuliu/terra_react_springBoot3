package com.ziheng.ai.infrastructure.client.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Spring AI 对话客户端实现
 *
 * 使用 Spring AI ChatClient 实现统一的 AI 对话调用接口。
 */
@Component
public class SpringAiChatClient implements AiChatClient {
    /**
     * 普通文本对话
     * @param chatClient 对话客户端
     * @param messages 消息列表
     * @return 回复内容
     */
    @Override
    public String chat(ChatClient chatClient, List<Message> messages) {
        return chatClient.prompt()
                .messages(messages)
                .call()
                .content();
    }

    /**
     * 流式文本对话
     * @param chatClient 对话客户端
     * @param messages 消息列表
     * @return 回复内容流
     */
    @Override
    public Flux<String> streamChat(ChatClient chatClient, List<Message> messages) {
        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content();
    }
}
