package com.ziheng.ai.capability.chat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.capability.chat.domain.dto.AiChatMessageQuery;
import com.ziheng.ai.capability.chat.domain.entity.AiChatMessage;
import com.ziheng.ai.capability.chat.domain.entity.AiChatSession;
import com.ziheng.ai.capability.chat.domain.vo.AiChatMessageVO;
import com.ziheng.ai.capability.chat.mapper.AiChatMessageMapper;
import com.ziheng.ai.capability.chat.service.AiChatMessageService;
import com.ziheng.ai.capability.chat.service.AiChatSessionService;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_chat_message(AI聊天消息表)】的数据库操作Service实现
* @createDate 2026-11-09 21:46:26
*/
@Service
public class AiChatMessageServiceImpl extends ServiceImpl<AiChatMessageMapper, AiChatMessage>
    implements AiChatMessageService{

    private final AiChatSessionService aiChatSessionService;

    public AiChatMessageServiceImpl(AiChatSessionService aiChatSessionService) {
        this.aiChatSessionService = aiChatSessionService;
    }

    /**
     * 保存用户消息
     * @param sessionId 会话ID
     * @param message 消息内容
     * @return 消息实体
     */
    @Override
    public AiChatMessage saveUserMessage(Long sessionId, String message) {
        AiChatMessage chatMessage = new AiChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole("USER");
        chatMessage.setContent(message);
        chatMessage.setCreateTime(new Date());
        baseMapper.insert(chatMessage);
        return chatMessage;
    }
    /**
     * 保存助手消息
     * @param sessionId 会话ID
     * @param content 消息内容
     * @param model 模型
     * @param provider 提供商
     * @return 消息实体
     */
    @Override
    public AiChatMessage saveAssistantMessage(Long sessionId, String content, AiModel model, AiProvider provider, Integer totalTokens) {
        AiChatMessage chatMessage = new AiChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole("ASSISTANT");
        chatMessage.setContent(content);
        chatMessage.setTokenCount(totalTokens);
        // 模型快照
        chatMessage.setModelId(model.getId());
        chatMessage.setModelCode(model.getModelCode());
        chatMessage.setModelName(model.getModelName());
        // 提供商快照
        chatMessage.setProviderId(provider.getId());
        chatMessage.setProviderName(provider.getProviderName());
        chatMessage.setCreateTime(new Date());
        baseMapper.insert(chatMessage);
        return chatMessage;
    }

    /**
     * 加载会话上下文
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @Override
    public List<Message> loadConversationContext(Long sessionId) {
        List<AiChatMessage> historyMessages = baseMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByAsc(AiChatMessage::getId)
                        .last("LIMIT 30"));
        List<Message> messages = new ArrayList<>();
        for (AiChatMessage message : historyMessages) {
            if ("USER".equals(message.getRole())) {
                messages.add(new UserMessage(message.getContent()));
                continue;
            }
            if ("ASSISTANT".equals(message.getRole())) {
                messages.add(new AssistantMessage(message.getContent()));
                continue;
            }
            throw new BusinessException("不支持的聊天消息类型");
        }
        return messages;
    }

    @Override
    public PageResult<AiChatMessageVO> pageMessages(AiChatMessageQuery query) {
        // 校验会话存在且属于当前登录用户
        Long userId = StpUtil.getLoginIdAsLong();
        AiChatSession session = aiChatSessionService.getById(query.getSessionId());
        if (session == null) {
            throw new BusinessException("聊天会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException("无权访问该聊天会话");
        }

        // 按id倒序分页：第1页为最新消息，pageNum递增即加载更早的消息
        Page<AiChatMessage> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, query.getSessionId())
                .orderByDesc(AiChatMessage::getId);
        Page<AiChatMessage> result = page(page, wrapper);

        // 翻转为本页时间正序（旧在上、新在下），前端可直接顺序渲染
        List<AiChatMessage> records = result.getRecords();
        List<AiChatMessageVO> rows = new ArrayList<>(records.size());
        for (int i = records.size() - 1; i >= 0; i--) {
            rows.add(AiChatMessageVO.from(records.get(i)));
        }
        return PageResult.of(result.getTotal(), rows);
    }
}
