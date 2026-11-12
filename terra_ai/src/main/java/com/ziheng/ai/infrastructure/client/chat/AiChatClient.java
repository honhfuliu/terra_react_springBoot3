package com.ziheng.ai.infrastructure.client.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 对话客户端统一接口
 *
 * 定义 AI 模型对话调用规范，
 * 屏蔽底层模型供应商和 SDK 的具体实现。
 */
public interface AiChatClient {
    /**
     * 普通文本对话
     */
    String chat(ChatClient chatClient, List<Message> messages);

    /**
     * 流式文本对话
     */
    Flux<String> streamChat(ChatClient chatClient, List<Message> messages);


}
