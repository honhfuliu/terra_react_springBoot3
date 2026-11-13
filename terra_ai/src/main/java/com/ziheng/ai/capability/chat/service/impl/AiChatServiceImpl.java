package com.ziheng.ai.capability.chat.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSendDTO;
import com.ziheng.ai.capability.chat.domain.entity.AiChatMessage;
import com.ziheng.ai.capability.chat.domain.entity.AiChatSession;
import com.ziheng.ai.capability.chat.domain.vo.AiChatMessageVO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSendVO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatStreamStartVO;
import com.ziheng.ai.capability.chat.event.AiChatStreamEvent;
import com.ziheng.ai.capability.chat.service.AiChatMessageService;
import com.ziheng.ai.capability.chat.service.AiChatService;
import com.ziheng.ai.capability.chat.service.AiChatSessionService;
import com.ziheng.ai.common.enums.AiModelType;
import com.ziheng.ai.config.AiProperties;
import com.ziheng.ai.infrastructure.calllog.domain.AiCallLog;
import com.ziheng.ai.infrastructure.calllog.service.AiCallLogService;
import com.ziheng.ai.infrastructure.client.chat.AiChatClient;
import com.ziheng.ai.infrastructure.client.chat.AiChatResponse;
import com.ziheng.ai.infrastructure.client.context.AiChatContext;
import com.ziheng.ai.infrastructure.client.factory.AiChatClientFactory;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelOptionVO;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.provider.service.AiProviderService;
import com.ziheng.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AiChatServiceImpl implements AiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final AiChatClientFactory aiChatClientFactory;

    private final AiProperties aiProperties;

    private final AiModelService aiModelService;
    private final AiProviderService aiProviderService;
    private final AiChatSessionService aiChatSessionService;
    private final AiChatMessageService aiChatMessageService;
    private final AiChatClient aiChatClient;
    private final AiCallLogService aiCallLogService;

    public AiChatServiceImpl(AiProperties aiProperties,
                             AiChatClientFactory aiChatClientFactory,
                             AiModelService aiModelService,
                             AiProviderService aiProviderService,
                             AiChatSessionService aiChatSessionService,
                             AiChatMessageService aiChatMessageService,
                             AiChatClient aiChatClient,
                             AiCallLogService aiCallLogService) {
        this.aiProperties = aiProperties;
        this.aiChatClientFactory = aiChatClientFactory;
        this.aiModelService = aiModelService;
        this.aiProviderService = aiProviderService;
        this.aiChatSessionService = aiChatSessionService;
        this.aiChatMessageService = aiChatMessageService;
        this.aiChatClient = aiChatClient;
        this.aiCallLogService = aiCallLogService;
    }


    /**
     * 列出可用的聊天模型
     *
     * @return 模型列表
     */
    @Override
    public List<AiModelOptionVO> listChatModels() {
        return aiModelService.listAvailableModels(AiModelType.CHAT.getCode());
    }

    /**
     * 发送消息
     *
     * @param dto 消息DTO
     * @return 消息VO
     */
    @Override
    public AiChatSendVO sendMessage(AiChatSendDTO dto) {
        // 1. 检查 AI 对话功能是否开启
        if (!aiProperties.getChat().isEnabled()) {
            throw new BusinessException("AI 对话功能已关闭");
        }
        // 2. 创建指定模型的 ChatClient
        AiChatContext aiChatContext = aiChatClientFactory.create(dto.getModelId());
        // 3. 获取当前登录用户 ID
        Long userId = StpUtil.getLoginIdAsLong();
        // 4. 获取或创建会话
        AiChatSession session = aiChatSessionService.getOrCreateSession(dto.getSessionId(), userId, dto.getMessage());
        // 5. 保存用户消息
        AiChatMessage userMessage = aiChatMessageService.saveUserMessage(session.getId(), dto.getMessage());
        //  6. 保存调用日志
        AiCallLog callLog = new AiCallLog();
        callLog.setUserId(userId);
        callLog.setSessionId(session.getId());
        callLog.setProviderId(aiChatContext.getProvider().getId());
        callLog.setProviderName(aiChatContext.getProvider().getProviderName());
        callLog.setModelId(aiChatContext.getModel().getId());
        callLog.setModelName(aiChatContext.getModel().getModelName());
        callLog.setCallType(AiModelType.CHAT.getCode());
        Long logId = aiCallLogService.createCallLog(callLog);

        try {
            // 7. 查询历史消息并转换为 Spring AI 消息
            List<Message> messages = aiChatMessageService.loadConversationContext(session.getId());
            // 8. 调用 AI 模型
            AiChatResponse chat = aiChatClient.chat(aiChatContext.getChatClient(), messages);
            // 9. 保存 AI 回复
            AiChatMessage assistantMessage = aiChatMessageService.saveAssistantMessage(session.getId(), chat.getContent(), aiChatContext.getModel(), aiChatContext.getProvider(),chat.getTotalTokens());
            // 10. 更新会话时间
            aiChatSessionService.updateSessionTime(session);
            // 11. 更新 AI 调用日志
            aiCallLogService.successCallLog(logId, assistantMessage.getId(), chat.getPromptTokens(), chat.getCompletionTokens(), chat.getTotalTokens());
            // 12. 构建并返回结果
            return new AiChatSendVO(session.getId(), session.getTitle(), AiChatMessageVO.from(userMessage), AiChatMessageVO.from(assistantMessage));
        } catch (Exception e) {
            aiCallLogService.failCallLog(logId, e.getMessage());
            throw e;

        }
    }

    /**
     * 流式消息
     *
     * @param dto 消息DTO
     * @return 消息流
     */
    @Override
    public Flux<AiChatStreamEvent> streamMessage(AiChatSendDTO dto) {
        return Flux.defer(() -> {
            // 检查 AI 对话功能是否开启
            if (!aiProperties.getChat().isEnabled()) {
                throw new BusinessException("AI 对话功能已关闭");
            }
            // 获取当前登录用户
            Long userId = StpUtil.getLoginIdAsLong();
            // 创建 AI 聊天上下文
            AiChatContext aiChatContext = aiChatClientFactory.create(dto.getModelId());
            // 创建或校验会话
            AiChatSession session = aiChatSessionService.getOrCreateSession(dto.getSessionId(), userId, dto.getMessage());
            // 保存用户消息
            AiChatMessage userMessage = aiChatMessageService.saveUserMessage(session.getId(), dto.getMessage());

            // 创建 AI 调用日志
            AiCallLog callLog = new AiCallLog();
            callLog.setUserId(userId);
            callLog.setSessionId(session.getId());
            callLog.setProviderId(aiChatContext.getProvider().getId());
            callLog.setProviderName(aiChatContext.getProvider().getProviderName());
            callLog.setModelId(aiChatContext.getModel().getId());
            callLog.setModelName(aiChatContext.getModel().getModelName());
            callLog.setCallType(AiModelType.CHAT.getCode());
            Long logId = aiCallLogService.createCallLog(callLog);

            // 加载历史对话上下文
            List<Message> messages = aiChatMessageService.loadConversationContext(session.getId());
            // 构建开始事件
            AiChatStreamStartVO startVO = new AiChatStreamStartVO(session.getId(), session.getTitle(), AiChatMessageVO.from(userMessage));
            // 累计 AI 完整回复
            StringBuilder answer = new StringBuilder();
            // 保存 AI 回复消息 ID
            AtomicReference<Long> messageIdRef = new AtomicReference<>();
            AtomicReference<AiChatResponse> usageRef = new AtomicReference<>(); // 保存 AI 调用使用情况
            // 构建开始事件
            AiChatStreamEvent startEvent = new AiChatStreamEvent("start", startVO);

            // 调用模型并转换为 chunk 事件
            Flux<AiChatStreamEvent> chunkFlux = aiChatClient.streamChat(aiChatContext.getChatClient(), messages)
                    .doOnNext(response -> {
                        if (response.getContent() != null) {
                            answer.append(response.getContent());
                        }
                        if (response.getTotalTokens() != null && response.getTotalTokens() > 0){
                            usageRef.set(response);
                        }
                    })
                    // 只推送有文本内容的 chunk
                    .filter(response -> response.getContent() != null && !response.getContent().isEmpty())
                    .map(chunk -> new AiChatStreamEvent("chunk", chunk.getContent()));
            // 先发送 start，再发送 chunk
            // 模型生成完成后，保存 AI 回复并发送 complete 事件
            Flux<AiChatStreamEvent> completeFlux = chunkFlux.concatWith(Mono.fromCallable(() -> {
                // 获取完整 AI 回复
                String content = answer.toString();
                // 保存 AI 回复
                AiChatMessage assistantMessage = aiChatMessageService.saveAssistantMessage(session.getId(), content, aiChatContext.getModel(), aiChatContext.getProvider(), usageRef.get().getTotalTokens());
                // 保存消息 ID，供调用日志使用
                messageIdRef.set(assistantMessage.getId());
                // 更新会话时间
                aiChatSessionService.updateSessionTime(session);
                // 转换为消息 VO
                AiChatMessageVO assistantMessageVO = AiChatMessageVO.from(assistantMessage);
                // 构建 complete 事件
                return new AiChatStreamEvent("complete", assistantMessageVO);
            }));

            // 按顺序发送 start、chunk、complete
            return Flux.concat(
                    Flux.just(startEvent),
                    completeFlux

            ).doOnComplete(() -> aiCallLogService.successCallLog(logId, messageIdRef.get(),
                            usageRef.get().getPromptTokens(),
                            usageRef.get().getCompletionTokens(),
                            usageRef.get().getTotalTokens())) // 正常完成
            // 异常
            .doOnError(ex -> {
                log.error("AI 流式聊天发生异常，logId={}", logId, ex);

                aiCallLogService.failCallLog(logId, ex.getMessage());
            })
            // 用户取消
            .doOnCancel(() -> {
                log.info("AI 流式调用已取消，logId={}", logId);
                aiCallLogService.cancelCallLog(logId);
            });

        }).onErrorResume(ex -> {
            // 记录服务端异常日志
            log.error("AI 流式聊天发生异常", ex);
            // 返回 error 事件
            return Flux.just(new AiChatStreamEvent("error", "AI 回复生成失败，请稍后重试"));
        });
    }
}
