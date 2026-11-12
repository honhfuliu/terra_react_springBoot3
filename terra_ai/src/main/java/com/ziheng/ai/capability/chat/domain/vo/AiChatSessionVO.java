package com.ziheng.ai.capability.chat.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * AI聊天会话列表视图对象
 */
@Data
public class AiChatSessionVO {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 更新时间
     */
    private Date updateTime;
}
