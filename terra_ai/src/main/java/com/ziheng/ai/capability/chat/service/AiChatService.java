package com.ziheng.ai.capability.chat.service;

import com.ziheng.ai.capability.chat.domain.dto.AiChatSendDTO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSendVO;
import com.ziheng.ai.capability.chat.event.AiChatStreamEvent;
import com.ziheng.ai.infrastructure.client.chat.AiChatResponse;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelOptionVO;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatService {
    /**
     * 获取可用的聊天模型列表（仅id和模型编码）
     *
     * @return 聊天模型选项列表
     */
    List<AiModelOptionVO> listChatModels();

    /**
     * 发送消息
     *
     * @param dto 消息DTO
     * @return 消息VO
     */
    AiChatSendVO sendMessage(AiChatSendDTO dto);

    /**
     * 流式发送聊天消息
     *
     * @param dto 聊天请求参数
     * @return 流式聊天事件
     */
    Flux<AiChatStreamEvent> streamMessage(AiChatSendDTO dto);


    /**
     * 内部业务调用模型（非流式）
     *
     * @param modelId 模型 ID
     * @param messages 消息列表
     * @return AI 响应
     */
    AiChatResponse chatOnce(Long modelId, List<Message> messages);
}
