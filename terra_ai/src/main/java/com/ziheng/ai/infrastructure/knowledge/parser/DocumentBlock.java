package com.ziheng.ai.infrastructure.knowledge.parser;


import com.ziheng.ai.infrastructure.knowledge.parser.enums.DocumentBlockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 文档内容块
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentBlock {

    /**
     * 块类型
     */
    private DocumentBlockType type;

    /**
     * 块内容
     */
    private String content;

    /**
     * 标题级别（仅标题块使用，例如 1、2、3）
     */
    private Integer headingLevel;

    /**
     * 代码语言（仅代码块使用，例如 java、sql）
     */
    private String language;

    /**
     * 所属标题路径
     * 例如：["第一章", "1.1 技术架构"]
     */
    private List<String> headingPath;

    /**
     * 扩展元数据
     */
    private Map<String, Object> metadata;
}

