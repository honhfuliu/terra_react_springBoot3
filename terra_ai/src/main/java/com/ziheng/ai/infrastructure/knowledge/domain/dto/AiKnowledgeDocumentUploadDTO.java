package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 知识库文档上传 DTO
 */
@Data
public class AiKnowledgeDocumentUploadDTO {

    /**
     * 知识库ID
     */
    private Long knowledgeId;

    /**
     * 上传文件
     */
    private MultipartFile file;
}