package com.ziheng.ai.capability.chat.service;

public interface AiChatService {
    /**
     * AI 对话
     *
     * @param message 用户输入内容
     * @return AI 回复内容
     */
    String chat(String message);
}
