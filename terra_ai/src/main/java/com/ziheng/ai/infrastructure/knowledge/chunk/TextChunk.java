package com.ziheng.ai.infrastructure.knowledge.chunk;

import com.ziheng.ai.infrastructure.knowledge.parser.enums.DocumentBlockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 文档文本切片
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextChunk {

    /**
     * 切片内容
     */
    private String content;

    /**
     * 切片索引
     */
    private Integer chunkIndex;

    /**
     * Token 数量
     */
    private Integer tokenCount;

    /**
     * 文档块类型
     */
    private DocumentBlockType type;

    /**
     * 所属标题路径
     */
    private List<String> headingPath;

    /**
     * 所属标题级别
     */
    private Integer headingLevel;

    /**
     * 代码语言
     */
    private String language;

    /**
     * 其他元数据
     */
    private Map<String, Object> metadata;
}
