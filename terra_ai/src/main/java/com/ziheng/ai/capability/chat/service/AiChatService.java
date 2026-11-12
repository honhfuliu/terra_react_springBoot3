package com.ziheng.ai.capability.chat.service;

import com.ziheng.ai.capability.chat.domain.dto.AiChatSendDTO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSendVO;
import com.ziheng.ai.capability.chat.event.AiChatStreamEvent;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelOptionVO;
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
}
