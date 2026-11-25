package com.ziheng.ai.infrastructure.knowledge.parser.docling.enums;

import lombok.Getter;

@Getter
public enum TaskType {
    CONVERT("convert"), // 转换
    CHUNK("chunk"); // 切块
    private final String value;
    TaskType(String value) {
        this.value = value;
    }

    public static TaskType fromValue(String value) {
        for (TaskType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的 Docling 任务类型：" + value);
    }
}
