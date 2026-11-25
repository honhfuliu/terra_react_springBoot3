package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

/**
 * 文档分片
 */
@Data
public class DoclingChunk {

    private String filename;

    @JsonProperty("chunk_index")
    private Integer chunkIndex;

    /**
     * 用于后续 Embedding 的文本
     */
    private String text;

    /**
     * 原始文本
     */
    @JsonProperty("raw_text")
    private String rawText;

    @JsonProperty("num_tokens")
    private Integer numTokens;

    private List<String> headings;

    private List<String> captions;

    @JsonProperty("doc_items")
    private List<String> docItems;

    @JsonProperty("page_numbers")
    private List<Integer> pageNumbers;

    private JsonNode metadata;
}
