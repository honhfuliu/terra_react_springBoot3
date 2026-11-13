package com.ziheng.ai.infrastructure.calllog.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI调用日志分页查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiCallLogQuery extends PageQuery {

    /**
     * 供应商ID
     */
    private Long providerId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 调用类型 CHAT聊天 EMBEDDING向量化
     */
    private String callType;

    /**
     * 调用状态，取值见 AiCallStatusEnum
     */
    private String status;

    /**
     * 调用开始时间，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
     */
    private String beginTime;

    /**
     * 调用结束时间，支持 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
}
