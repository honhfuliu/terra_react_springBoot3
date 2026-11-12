package com.ziheng.ai.capability.chat.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 聊天流事件
 */
@Data
@AllArgsConstructor
public class AiChatStreamEvent {

    /**
     * 事件类型：start、chunk、complete、error
     */
    private String type;

    /**
     * 事件数据
     */
    private Object data;
}
