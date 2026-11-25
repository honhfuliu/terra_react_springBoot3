package com.ziheng.ai.infrastructure.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识库文档资源清理类型
 */
@Getter
@AllArgsConstructor
public enum AiDocumentCleanupType {

    /**
     * 文件删除：原始文件、Markdown、Docling JSON
     */
    FILE_DELETE("FILE_DELETE", "文件删除"),

    /**
     * Chunk 和 Embedding 数据删除
     */
    CHUNK_EMBEDDING_DELETE("CHUNK_EMBEDDING_DELETE", "Chunk和Embedding删除");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;
}
