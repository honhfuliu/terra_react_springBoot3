package com.ziheng.ai.capability.chat.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 聊天流开始事件数据
 */
@Data
@AllArgsConstructor
public class AiChatStreamStartVO {

    /**
     * 会话 ID
     */
    private Long sessionId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 用户消息
     */
    private AiChatMessageVO userMessage;
}

