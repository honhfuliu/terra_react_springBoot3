package com.ziheng.ai.capability.chat.controller;

import com.ziheng.ai.capability.chat.service.AiChatService;
import com.ziheng.common.core.domain.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping("/ai")
    private R<String> chatAI(@RequestParam(value = "msg", defaultValue = "你是谁") String msg){
        return R.ok(aiChatService.chat(msg));
    }

}
