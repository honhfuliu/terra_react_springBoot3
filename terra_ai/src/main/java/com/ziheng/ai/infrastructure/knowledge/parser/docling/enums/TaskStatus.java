package com.ziheng.ai.infrastructure.knowledge.parser.docling.enums;

import lombok.Getter;

/**
 * Docling 任务状态
 */
@Getter
public enum TaskStatus {
    PENDING("pending"),  // 排队中，尚未开始
    STARTED("started"),  // 开始执行
    FAILURE("failure"),  // 执行失败
    SUCCESS("success"),  // 执行成功
    PARTIAL_SUCCESS("partial_success"),  // 部分成功
    SKIPPED("skipped");  // 跳过

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的 Docling 任务状态：" + value);
    }

}
