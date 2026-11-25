package com.ziheng.ai.infrastructure.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentTaskStatus {

    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "处理成功"),
    FAILED("FAILED", "处理失败"),
    RETRYING("RETRYING", "等待重试");

    private final String code;
    private final String label;

    /**
     * 根据状态码获取枚举
     */
    public static DocumentTaskStatus fromCode(String code) {
        for (DocumentTaskStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}