package com.ziheng.ai.infrastructure.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档任务类型
 */
@Getter
@AllArgsConstructor
public enum DocumentTaskType {

    PARSE("PARSE", "文档解析"),
    CHUNK("CHUNK", "文档切片"),
    EMBEDDING("EMBEDDING", "向量化");

    private final String code;
    private final String label;

    /**
     * 根据状态值获取枚举
     */
    public static DocumentTaskType fromCode(String code) {
        for (DocumentTaskType status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
