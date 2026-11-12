package com.ziheng.ai.capability.chat.controller;

import com.ziheng.ai.capability.chat.domain.dto.AiChatMessageQuery;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSendDTO;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSessionQuery;
import com.ziheng.ai.capability.chat.domain.dto.AiChatSessionRenameDTO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatMessageVO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSendVO;
import com.ziheng.ai.capability.chat.domain.vo.AiChatSessionVO;
import com.ziheng.ai.capability.chat.event.AiChatStreamEvent;
import com.ziheng.ai.capability.chat.service.AiChatMessageService;
import com.ziheng.ai.capability.chat.service.AiChatService;
import com.ziheng.ai.capability.chat.service.AiChatSessionService;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelOptionVO;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai/chat")
@Tag(name = "AI聊天接口")
public class AiChatController {

    private final AiChatService aiChatService;

    private final AiChatSessionService aiChatSessionService;

    private final AiChatMessageService aiChatMessageService;

    public AiChatController(AiChatService aiChatService, AiChatSessionService aiChatSessionService,
                            AiChatMessageService aiChatMessageService) {
        this.aiChatService = aiChatService;
        this.aiChatSessionService = aiChatSessionService;
        this.aiChatMessageService = aiChatMessageService;
    }


    @GetMapping("/models")
    @Operation(summary = "获取可用聊天模型列表")
    public R<List<AiModelOptionVO>> getAIModels(){
        return R.ok(aiChatService.listChatModels());
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public R<AiChatSendVO> sendMessage(@RequestBody AiChatSendDTO dto){
        return R.ok(aiChatService.sendMessage(dto));
    }

    /**
     * AI 流式聊天
     *
     * @param dto 聊天请求参数
     * @return SSE 事件流
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式聊天")
    public Flux<ServerSentEvent<AiChatStreamEvent>> streamMessage(@RequestBody AiChatSendDTO dto) {
        return aiChatService.streamMessage(dto)
                .map(event -> ServerSentEvent
                        .<AiChatStreamEvent>builder()
                        .event(event.getType())
                        .data(event)
                        .build());
    }

    /**
     * 会话分页查询
     */
    @GetMapping("/session/page")
    @Operation(summary = "分页查询聊天会话")
    public R<PageResult<AiChatSessionVO>> sessions(AiChatSessionQuery query) {
        return R.ok(aiChatSessionService.pageSessions(query));
    }

    /**
     * 会话消息分页查询（第1页最新，向上滚动继续加载更早消息）
     */
    @GetMapping("/message/page")
    @Operation(summary = "分页查询会话消息")
    public R<PageResult<AiChatMessageVO>> messages(@Valid AiChatMessageQuery query) {
        return R.ok(aiChatMessageService.pageMessages(query));
    }

    /**
     * 修改会话名称
     */
    @PostMapping("/session/title")
    @Operation(summary = "修改会话名称")
    public R<Void> renameSession(@Valid @RequestBody AiChatSessionRenameDTO dto) {
        aiChatSessionService.renameSession(dto);
        return R.ok();
    }

    /**
     * 删除会话（连同会话下所有聊天消息一起物理删除）
     */
    @PostMapping("/session/delete/{sessionId}")
    @Operation(summary = "删除会话")
    public R<Void> deleteSession(@PathVariable("sessionId") Long sessionId) {
        aiChatSessionService.deleteSession(sessionId);
        return R.ok();
    }
}
