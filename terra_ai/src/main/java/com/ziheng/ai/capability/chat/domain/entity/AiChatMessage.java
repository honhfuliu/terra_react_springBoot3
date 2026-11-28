package com.ziheng.ai.capability.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI聊天消息表
 * @TableName ai_chat_message
 */
@TableName(value ="ai_chat_message")
@Data
public class AiChatMessage {
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 消息角色 USER/ASSISTANT
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 模型编码快照
     */
    private String modelCode;

    /**
     * 模型名称快照
     */
    private String modelName;
    /**
     * 提供商ID
     */
    private Long providerId;

    /**
     * 提供商名称快照
     */
    private String providerName;

    /**
     * Token数量
     */
    private Integer tokenCount;

    /**
     * 消息状态：0处理中 1成功 2失败
     */
    private Integer status;

    /**
     * RAG 检索来源，JSON 格式
     */
    private String sources;

    /**
     * 扩展元数据，JSON 格式
     */
    private String metadata;

    /**
     * 创建时间
     */
    private Date createTime;

}