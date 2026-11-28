package com.ziheng.ai.capability.chat.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI聊天会话表
 * @TableName ai_chat_session
 */
@TableName(value ="ai_chat_session")
@Data
public class AiChatSession {
    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话标题
     */
    private String title;

    /** 聊天类型：CHAT / RAG */
    private String chatType;

    /** 知识库ID，普通聊天为空 */
    private Long knowledgeId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}