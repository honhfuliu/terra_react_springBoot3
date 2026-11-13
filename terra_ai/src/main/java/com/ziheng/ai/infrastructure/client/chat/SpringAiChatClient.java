package com.ziheng.ai.infrastructure.client.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
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
    private final Logger log = LoggerFactory.getLogger(SpringAiChatClient.class);
    /**
     * 普通文本对话
     * @param chatClient 对话客户端
     * @param messages 消息列表
     * @return 回复内容
     */
    @Override
    public AiChatResponse chat(ChatClient chatClient, List<Message> messages) {
        ChatResponse response = chatClient.prompt()
                .messages(messages)
                .call()
                .chatResponse();
        AiChatResponse result = new AiChatResponse();
        if (response != null) {
            result.setContent(response.getResult().getOutput().getText());
            // 获取 Token 使用量
            Usage usage = response.getMetadata().getUsage();
            if (usage != null) {
                result.setPromptTokens(usage.getPromptTokens());
                result.setCompletionTokens(usage.getCompletionTokens());
                result.setTotalTokens(usage.getTotalTokens());
            }
        }
        return result;
    }

    /**
     * 流式文本对话
     * @param chatClient 对话客户端
     * @param messages 消息列表
     * @return 回复内容流
     */
    @Override
    public Flux<AiChatResponse> streamChat(ChatClient chatClient, List<Message> messages) {
        return chatClient.prompt()
                .messages(messages)
                .stream()
                .chatResponse()
                .map(response -> {
                    AiChatResponse result = new AiChatResponse();
                    // 获取文本内容
                    if (response != null) {
                        // 获取文本内容
                        if (response.getResult() != null && response.getResult().getOutput() != null) {
                            result.setContent(response.getResult().getOutput().getText());
                        }
                        // 获取 Token 使用量
                        Usage usage = response.getMetadata().getUsage();
                        if (usage != null && usage.getTotalTokens() != null && usage.getTotalTokens() > 0) {
                            log.info("Token Usage: promptTokens={}, completionTokens={}, totalTokens={}",
                                    usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
                            result.setPromptTokens(usage.getPromptTokens());
                            result.setCompletionTokens(usage.getCompletionTokens());
                            result.setTotalTokens(usage.getTotalTokens());
                        }
                    }
                    return result;
                });
    }
}
