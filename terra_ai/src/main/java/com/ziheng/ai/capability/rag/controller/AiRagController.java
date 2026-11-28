package com.ziheng.ai.capability.rag.controller;

import com.ziheng.ai.capability.rag.domain.dto.AiRagRequest;
import com.ziheng.ai.capability.rag.domain.vo.AiRagResponse;
import com.ziheng.ai.capability.rag.service.AiRagService;
import com.ziheng.common.core.domain.R;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 知识库问答控制器
 *
 * @author
 */
@RestController
@RequestMapping("/ai/rag")
public class AiRagController {

    private final AiRagService aiRagService;

    public AiRagController(AiRagService aiRagService) {
        this.aiRagService = aiRagService;
    }

    /**
     * RAG 知识库问答
     *
     * @param request 问答请求
     * @return 问答结果
     */
    @PostMapping("/chat")
    public R<AiRagResponse> chat(@Valid @RequestBody AiRagRequest request) {
        AiRagResponse response = aiRagService.chat(request);

        return R.ok(response);
    }
}
