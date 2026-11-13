package com.ziheng.ai.infrastructure.calllog.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * AI调用日志列表视图对象
 */
@Data
public class AiCallLogVO {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 供应商
     */
    private String providerName;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 调用类型 CHAT聊天 EMBEDDING向量化
     */
    private String callType;

    /**
     * Prompt Tokens
     */
    private Integer promptTokens;

    /**
     * Completion Tokens
     */
    private Integer completionTokens;

    /**
     * Total Tokens
     */
    private Integer totalTokens;

    /**
     * 调用耗时 毫秒
     */
    private Long duration;

    /**
     * 调用状态 0进行中 1成功 2失败 3已取消
     */
    private String status;

    /**
     * 调用时间
     */
    private Date createTime;

    /**
     * 聊天会话ID
     */
    private Long sessionId;

    /**
     * AI回复消息ID
     */
    private Long messageId;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 异常信息
     */
    private String errorMessage;
}
