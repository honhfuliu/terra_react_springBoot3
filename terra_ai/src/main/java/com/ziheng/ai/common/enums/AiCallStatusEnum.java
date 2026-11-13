package com.ziheng.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI调用日志状态
 * 0-进行中 1-成功 2-失败 3-已取消
 */
@Getter
@AllArgsConstructor
public enum AiCallStatusEnum {

    RUNNING("0", "进行中"),
    SUCCESS("1", "成功"),
    FAILED("2", "失败"),
    CANCELLED("3", "已取消");

    private final String code;
    private final String description;
}
