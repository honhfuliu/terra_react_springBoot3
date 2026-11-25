package com.ziheng.ai.infrastructure.knowledge.parser.impl;

import com.ziheng.ai.infrastructure.knowledge.parser.DocumentBlock;
import com.ziheng.ai.infrastructure.knowledge.parser.DocumentParser;
import com.ziheng.ai.infrastructure.knowledge.parser.ParsedDocument;
import com.ziheng.ai.infrastructure.knowledge.parser.enums.DocumentBlockType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文本文档解析器
 */
public class TextDocumentParser implements DocumentParser {

    @Override
    public ParsedDocument parse(String filePath) {
        try {
            // 1. 读取 TXT 原始内容
            String text = Files.readString(
                    Path.of(filePath),
                    StandardCharsets.UTF_8
            );

            // 2. 按段落解析
            List<DocumentBlock> blocks = parseParagraphs(text);

            // 3. 构建解析结果
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileType", "txt");
            metadata.put("filePath", filePath);
            metadata.put("blockCount", blocks.size());

            return ParsedDocument.builder()
                    .blocks(blocks)
                    .pageCount(null)
                    .metadata(metadata)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("TXT 文件解析失败：" + filePath, e);
        }
    }

    /**
     * 将连续非空行合并为一个段落 Block。
     */
    private List<DocumentBlock> parseParagraphs(String text) {
        List<DocumentBlock> blocks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return blocks;
        }

        String[] lines = text.split("\\R", -1);

        StringBuilder paragraph = new StringBuilder();

        for (String line : lines) {

            // 空行代表段落结束
            if (line.isBlank()) {
                addParagraphBlock(blocks, paragraph);
                continue;
            }

            // 保留段落内部的换行
            if (!paragraph.isEmpty()) {
                paragraph.append("\n");
            }

            paragraph.append(line);
        }

        // 处理文件末尾没有空行的情况
        addParagraphBlock(blocks, paragraph);

        return blocks;
    }

    /**
     * 将当前段落转换为 DocumentBlock。
     */
    private void addParagraphBlock(
            List<DocumentBlock> blocks,
            StringBuilder paragraph
    ) {
        String content = paragraph.toString();

        if (!content.isBlank()) {
            blocks.add(DocumentBlock.builder()
                    .type(DocumentBlockType.PARAGRAPH)
                    .content(content)
                    .headingPath(List.of())
                    .metadata(new HashMap<>())
                    .build());
        }

        paragraph.setLength(0);
    }

    @Override
    public boolean supports(String fileExt) {
        return fileExt != null && "txt".equalsIgnoreCase(fileExt.replaceFirst("^\\.", ""));
    }
}
