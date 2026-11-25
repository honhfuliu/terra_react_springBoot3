package com.ziheng.ai.infrastructure.knowledge.parser.impl;

import com.ziheng.ai.infrastructure.knowledge.parser.DocumentBlock;
import com.ziheng.ai.infrastructure.knowledge.parser.DocumentParser;
import com.ziheng.ai.infrastructure.knowledge.parser.ParsedDocument;
import com.ziheng.ai.infrastructure.knowledge.parser.enums.DocumentBlockType;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tika文档解析器
 */
public class TikaDocumentParser implements DocumentParser {
    private static final Logger log = LoggerFactory.getLogger(TikaDocumentParser.class);

    /**
     * Tika 自动检测解析器。
     */
    private final AutoDetectParser parser = new AutoDetectParser();

    /**
     * 文件类型检测器。
     */
    private final Tika tika = new Tika();

    /**
     * 解析文档
     *
     * @param filePath 文档存储路径
     * @return 文档解析结果
     */
    @Override
    public ParsedDocument parse(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("文件不存在：" + filePath);
        }

        Metadata metadata = new Metadata();
        String content;

        try (InputStream inputStream = Files.newInputStream(path)) {
            // 1. 设置文件名，辅助 Tika 判断文件类型
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, path.getFileName().toString());
            // 2. 创建文本处理器
            BodyContentHandler handler = new BodyContentHandler(-1);
            // 3. 自动识别并解析文件
            parser.parse(inputStream, handler, metadata);
            // 4. 获取提取后的文本
            content = handler.toString();

        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException("文档解析失败：" + filePath, e);
        }
        // 5. 将提取的文本转换为 DocumentBlock
        List<DocumentBlock> blocks = parseParagraphs(content);

        // 6. 收集元数据
        Map<String, Object> documentMetadata = new HashMap<>();

        documentMetadata.put("fileName", path.getFileName().toString());
        documentMetadata.put("contentType", metadata.get(Metadata.CONTENT_TYPE));
        documentMetadata.put("title", metadata.get(TikaCoreProperties.TITLE));
        documentMetadata.put("author", metadata.get(TikaCoreProperties.CREATOR));
        documentMetadata.put("blockCount", blocks.size());

        // PDF 页数可能无法获取，获取不到时返回 null
        Integer pageCount = null;

        String pages = metadata.get("xmpTPg:NPages");

        if (pages != null) {
            try {
                pageCount = Integer.parseInt(pages);
            } catch (NumberFormatException ignored) {
                // 不使用不可靠的页数
            }
        }

        return ParsedDocument.builder()
                .blocks(blocks)
                .pageCount(pageCount)
                .metadata(documentMetadata)
                .build();

    }

    /**
     * 将提取的文本按段落转换为 DocumentBlock。
     */
    private List<DocumentBlock> parseParagraphs(String content) {
        List<DocumentBlock> blocks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return blocks;
        }
        // Tika 会统一提取不同格式的文本。
        // 连续空白行作为段落边界。
        String[] paragraphs = content.split("(?:\\R[\\t ]*){2,}");
        for (String paragraph : paragraphs) {
            String text = paragraph.strip();
            if (text.isEmpty()) {
                continue;
            }
            blocks.add(DocumentBlock.builder()
                    .type(DocumentBlockType.PARAGRAPH)
                    .content(text)
                    .headingPath(List.of())
                    .metadata(new HashMap<>())
                    .build());
        }

        return blocks;
    }

    /**
     * 是否支持解析该文件类型
     * @param fileExt 文件扩展名
     * @return 是否支持
     */
    @Override
    public boolean supports(String fileExt) {
        return false;
    }



}
