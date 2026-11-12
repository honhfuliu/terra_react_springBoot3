package com.ziheng.ai.capability.chat.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 聊天发送消息响应对象
 */
@Data
@AllArgsConstructor
public class AiChatSendVO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 用户发送的消息
     */
    private AiChatMessageVO userMessage;

    /**
     * AI 回复的消息
     */
    private AiChatMessageVO assistantMessage;
}
