package com.ziheng.ai.infrastructure.vector.controller;

import com.ziheng.ai.infrastructure.vector.domain.dto.AiDocumentChunkQuery;
import com.ziheng.ai.infrastructure.vector.domain.vo.AiDocumentChunkVO;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/knowledge/chunk")
@Tag(name = "AI知识库文档分块管理")
public class AiDocumentChunkController {

    private final AiDocumentChunkService chunkService;

    public AiDocumentChunkController(AiDocumentChunkService chunkService) {
        this.chunkService = chunkService;
    }

    /**
     * 分页查询文档分块
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询文档分块")
    public R<PageResult<AiDocumentChunkVO>> page(@Valid AiDocumentChunkQuery query) {
        return R.ok(chunkService.page(query));
    }
}
