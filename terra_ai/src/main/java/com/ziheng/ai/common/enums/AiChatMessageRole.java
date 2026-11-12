package com.ziheng.ai.common.enums;

import lombok.Getter;

/**
 * AI聊天消息角色
 *
 * @author ziheng
 */
@Getter
public enum AiChatMessageRole {

    USER("USER", "用户"),

    ASSISTANT("ASSISTANT", "AI");

    private final String code;

    private final String label;

    AiChatMessageRole(String code, String label) {
        this.code = code;
        this.label = label;
    }

}
