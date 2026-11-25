package com.ziheng.ai.infrastructure.knowledge.parser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 解析后的文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDocument {

    /**
     * 文档内容块
     */
    private List<DocumentBlock> blocks;

    /**
     * 页数（PDF、Word 等格式可以提供）
     */
    private Integer pageCount;

    /**
     * 文档元数据
     */
    private Map<String, Object> metadata;
}
