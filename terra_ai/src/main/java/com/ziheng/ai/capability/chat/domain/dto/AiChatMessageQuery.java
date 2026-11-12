package com.ziheng.ai.capability.chat.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI聊天消息分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiChatMessageQuery extends PageQuery {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 首次进入会话默认加载最新30条，前端可传pageSize覆盖
     */
    public AiChatMessageQuery() {
        setPageSize(30);
    }
}
