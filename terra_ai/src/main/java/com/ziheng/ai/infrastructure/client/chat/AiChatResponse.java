package com.ziheng.ai.infrastructure.client.chat;

import lombok.Data;
/**
 * AI 对话回复
 */
@Data
public class AiChatResponse {

    /**
     * AI 回复内容
     */
    private String content;

    /**
     * 输入 Token 数量
     */
    private Integer promptTokens;

    /**
     * 输出 Token 数量
     */
    private Integer completionTokens;

    /**
     * 总 Token 数量
     */
    private Integer totalTokens;
}