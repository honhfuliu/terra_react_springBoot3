package com.ziheng.ai.infrastructure.knowledge.chunk;

import com.ziheng.ai.infrastructure.knowledge.parser.ParsedDocument;

import java.util.List;

/**
 * 文档文本切片器
 */
public interface TextChunker {

    /**
     * 对解析后的文档进行文本切片
     *
     * @param document     解析后的文档
     * @param chunkSize    目标切片大小
     * @param chunkOverlap 切片重叠大小
     * @return 文本切片集合
     */
    List<TextChunk> split(ParsedDocument document, Integer chunkSize, Integer chunkOverlap);
}
