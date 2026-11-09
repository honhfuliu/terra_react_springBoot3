package com.ziheng.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AiModelType {

    CHAT("CHAT", "聊天模型"),

    EMBEDDING("EMBEDDING", "向量模型"),

    RERANK("RERANK", "重排序模型");

    private final String code;

    private final String description;
}