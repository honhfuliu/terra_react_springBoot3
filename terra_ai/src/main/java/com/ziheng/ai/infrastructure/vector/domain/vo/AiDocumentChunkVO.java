package com.ziheng.ai.infrastructure.vector.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * AI知识库文档分块分页查询返回
 */
@Data
public class AiDocumentChunkVO {

    /**
     * 分块ID
     */
    private Long id;

    /**
     * 文档内分块顺序
     */
    private Integer chunkIndex;

    /**
     * 分块文本内容
     */
    private String content;

    /**
     * Token 数量
     */
    private Integer tokenCount;

    /**
     * 页码
     */
    private List<Integer> pageNumbers;
}
