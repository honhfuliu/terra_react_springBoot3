package com.ziheng.ai.capability.chat.domain.vo;



import com.ziheng.ai.capability.chat.domain.entity.AiChatMessage;
import lombok.Data;

import java.util.Date;

/**
 * AI 聊天消息响应对象
 */
@Data
public class AiChatMessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息角色 USER / ASSISTANT
     */
    private String role;

    /**
     * 使用的模型ID
     */
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * 消息时间
     */
    private Date createTime;


    public static AiChatMessageVO from(AiChatMessage message) {
        if (message == null) {
            return null;
        }
        AiChatMessageVO vo = new AiChatMessageVO();
        vo.setId(message.getId());
        vo.setContent(message.getContent());
        vo.setRole(message.getRole());
        vo.setModelId(message.getModelId());
        vo.setModelName(message.getModelName());
        vo.setProviderName(message.getProviderName());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
