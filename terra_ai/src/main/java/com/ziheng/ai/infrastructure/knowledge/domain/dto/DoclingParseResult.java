package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * Docling 解析结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoclingParseResult {
    private String markdownPath;
    private String jsonPath;
}
