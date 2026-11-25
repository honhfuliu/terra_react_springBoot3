package com.ziheng.ai.infrastructure.knowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI 文档解析状态
 */
@Getter
@RequiredArgsConstructor
public enum AiDocumentProcessStatus {

    PENDING("PENDING", "待解析"),
    PROCESSING("PROCESSING", "解析中"),
    RETRYING("RETRYING", "重试中"),
    SUCCESS("SUCCESS", "解析成功"),
    FAILED("FAILED", "解析失败");

    /**
     * 状态值
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态值获取枚举
     */
    public static AiDocumentProcessStatus fromCode(String code) {
        for (AiDocumentProcessStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}