package com.ziheng.ai.infrastructure.client.chat;

/**
 * AI 对话客户端统一接口
 *
 * 定义 AI 模型对话调用规范，
 * 屏蔽底层模型供应商和 SDK 的具体实现。
 */
public interface AiChatClient {

    /**
     * 普通文本对话
     *
     * @param message 用户输入内容
     * @return AI 模型生成的回答
     */
    String chat(String message);


}
