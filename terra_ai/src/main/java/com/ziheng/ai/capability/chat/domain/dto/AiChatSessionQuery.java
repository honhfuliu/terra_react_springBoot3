package com.ziheng.ai.capability.chat.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI聊天会话分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatSessionQuery extends PageQuery {

    /**
     * 会话标题（模糊查询）
     */
    private String title;
}
