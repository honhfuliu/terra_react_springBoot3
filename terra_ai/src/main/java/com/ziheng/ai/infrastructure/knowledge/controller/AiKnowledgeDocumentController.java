package com.ziheng.ai.infrastructure.knowledge.controller;

import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeDocumentQuery;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeDocumentUploadDTO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentDetailVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDocumentVO;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeDocumentService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ai/knowledge/document")
@Tag(name = "AI知识文档管理")
public class AiKnowledgeDocumentController {

    private final AiKnowledgeDocumentService aiKnowledgeDocumentService;

    public AiKnowledgeDocumentController(AiKnowledgeDocumentService aiKnowledgeDocumentService) {
        this.aiKnowledgeDocumentService = aiKnowledgeDocumentService;
    }

    /**
     * 分页查询知识库文档
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询知识库文档")
    public R<PageResult<AiKnowledgeDocumentVO>> page(@Valid AiKnowledgeDocumentQuery query) {
        return R.ok(aiKnowledgeDocumentService.page(query));
    }

    /**
     * 根据ID查询知识库文档详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询知识库文档详情")
    public R<AiKnowledgeDocumentDetailVO> detail(@PathVariable("id") Long id) {
        return R.ok(aiKnowledgeDocumentService.detail(id));
    }

    /**
     * 删除知识库文档
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除知识库文档")
    @OperateLog(module = OperateModule.AI_KNOWLEDGE, idSource = IdSource.PATH,
            type = OperateType.DELETE, service = AiKnowledgeDocumentService.class)
    public R<Void> remove(@PathVariable("id") Long id) {
        aiKnowledgeDocumentService.delete(id);
        return R.ok();
    }

    /**
     * 上传知识库文档
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @Operation(summary = "上传知识库文档")
    public R<Void> upload(@RequestParam("knowledgeId") Long knowledgeId, @RequestParam("file") MultipartFile file) {
        AiKnowledgeDocumentUploadDTO dto = new AiKnowledgeDocumentUploadDTO();
        dto.setKnowledgeId(knowledgeId);
        dto.setFile(file);
        aiKnowledgeDocumentService.upload(dto);
        return R.ok();
    }

    /**
     * 下载知识库文档原始文件
     */
    @GetMapping("/file")
    @Operation(summary = "下载知识库文档原始文件")
    public void downloadFile(@RequestParam("documentId") Long documentId, HttpServletResponse response) {
        aiKnowledgeDocumentService.downloadFile(documentId, response);
    }

    /**
     * 获取文档解析后的 Markdown 文件（用于前端预览）
     */
    @GetMapping("/markdown")
    @Operation(summary = "获取文档解析后的 Markdown 文件")
    public void downloadMarkdown(@RequestParam("documentId") Long documentId, HttpServletResponse response) {
        aiKnowledgeDocumentService.downloadMarkdown(documentId, response);
    }

}
