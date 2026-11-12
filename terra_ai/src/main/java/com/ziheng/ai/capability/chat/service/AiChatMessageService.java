package com.ziheng.ai.capability.chat.service;

import com.ziheng.ai.capability.chat.domain.dto.AiChatMessageQuery;
import com.ziheng.ai.capability.chat.domain.entity.AiChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.ai.capability.chat.domain.vo.AiChatMessageVO;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.common.core.domain.PageResult;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_chat_message(AI聊天消息表)】的数据库操作Service
* @createDate 2026-11-09 21:46:26
*/
public interface AiChatMessageService extends IService<AiChatMessage> {

    /**
     * 保存用户消息
     * @param sessionId 会话ID
     * @param message 消息内容
     * @return 消息实体
     */
    AiChatMessage saveUserMessage(Long sessionId, String message);


    /**
     * 保存助手消息
      * @param sessionId 会话ID
      * @param content 消息内容
      * @param model 模型
      * @param provider 提供商
      * @return 消息实体
      */
    AiChatMessage saveAssistantMessage(Long sessionId, String content, AiModel model, AiProvider provider);

    /**
     * 加载会话上下文
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<Message> loadConversationContext(Long sessionId);

    /**
     * 分页查询会话消息（第1页最新，后续页更早）
     *
     * @param query 查询参数
     * @return 分页结果，rows按时间正序（旧在上、新在下）
     */
    PageResult<AiChatMessageVO> pageMessages(AiChatMessageQuery query);

}
