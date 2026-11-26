package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Docling 解析结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoclingParseResult {
    private String markdownPath;
    private String jsonPath;
    /**
     * 文档页数
     */
    private Integer pageCount;
}
