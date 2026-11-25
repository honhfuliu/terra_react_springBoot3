package com.ziheng.ai.infrastructure.knowledge.parser.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.ai.infrastructure.knowledge.parser.DocumentBlock;
import com.ziheng.ai.infrastructure.knowledge.parser.DocumentParser;
import com.ziheng.ai.infrastructure.knowledge.parser.ParsedDocument;
import com.ziheng.ai.infrastructure.knowledge.parser.enums.DocumentBlockType;
import com.ziheng.ai.infrastructure.knowledge.parser.enums.PdfElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * PDF解析器
 */
@Component
public class PdfJsonParser implements DocumentParser {
    private static final Logger log = LoggerFactory.getLogger(PdfJsonParser.class);

    private static class ParseContext {
        private final Deque<HeadingNode> headingStack = new ArrayDeque<>();
        private int blockIndex;
    }

    private record HeadingNode(int level, String title) {}


    private final ObjectMapper objectMapper;

    public PdfJsonParser() {
        this.objectMapper = new ObjectMapper();
    }
    // 判断是否支持解析 JSON
    @Override
    public boolean supports(String fileExt) {
        return "json".equalsIgnoreCase(fileExt);
    }

    // 解析 JSON 文件
    @Override
    public ParsedDocument parse(String filePath) {
        try {
            JsonNode root = objectMapper.readTree(Path.of(filePath).toFile());

            // 1. 读取 PDF 基础信息
            Integer pageCount = root.path("number of pages").isInt()
                    ? root.path("number of pages").asInt()
                    : 0;

            // 2. 读取根节点元数据
            Map<String, Object> metadata = parseMetadata(root);

            // 3. 创建本次解析的上下文
            ParseContext context = new ParseContext();
            // 4. 遍历并解析内容
            List<DocumentBlock> blocks = parseBlocks(
                    root.path("kids"),
                    context
            );
            return ParsedDocument.builder()
                    .pageCount(pageCount)
                    .metadata(metadata)
                    .blocks(blocks)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("PDF JSON 文件解析失败：" + filePath, e);
        }
    }

    // 解析根节点元数据
    private Map<String, Object> parseMetadata(JsonNode root) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", root.path("file name").asText(null));
        metadata.put("author", root.path("author").asText(null));
        metadata.put("title", root.path("title").asText(null));
        metadata.put("creationDate", root.path("creation date").asText(null));
        metadata.put("modificationDate", root.path("modification date").asText(null));
        return metadata;
    }


    // 遍历 kids
    private List<DocumentBlock> parseBlocks(JsonNode kids, ParseContext context) {
        List<DocumentBlock> blocks = new ArrayList<>();
        if (kids == null || !kids.isArray()) {
            return blocks;
        }
        for (JsonNode node : kids) {
            DocumentBlock block = parseBlock(node, context);
            if (block != null) {
                blocks.add(block);
                context.blockIndex++;
            }
        }
        return blocks;
    }

    // 根据 type 分发解析
    private DocumentBlock parseBlock(JsonNode node, ParseContext context) {
        String type = node.path("type").asText(null);

        PdfElementType elementType = PdfElementType.fromValue(type);

        if (elementType == null) {
            log.error("发现未知 PDF 元素类型：{}，元素 ID：{}", type, node.path("id").asText());
            return null;
        }

        return switch (elementType) {
            case HEADING -> parseHeading(node, context);
            case PARAGRAPH, CAPTION -> parseParagraph(node, context);
            case LIST -> parseList(node, context);
            // 其他类型后续逐步实现
            default -> {
                log.error("暂未实现 PDF 元素类型：{}，元素 ID：{}", type, node.path("id").asText());
                yield null;
            }
        };
    }

    // 解析标题
    private DocumentBlock parseHeading(JsonNode node, ParseContext context) {
        // 1. 获取标题文本
        String content = node.path("content").asText("");

        // 2. 获取标题级别
        JsonNode levelNode = node.path("heading level");
        if (!levelNode.isInt() || levelNode.asInt() <= 0) {
            throw new IllegalArgumentException("PDF 标题缺少有效的 heading level，元素 ID：" + node.path("id").asText());
        }

        int headingLevel = levelNode.asInt();

        // 3. 移除当前级别及以下的旧标题
        while (!context.headingStack.isEmpty()
                && context.headingStack.peekLast().level() >= headingLevel) {
            context.headingStack.removeLast();
        }

        // 4. 将当前标题加入栈
        context.headingStack.addLast(
                new HeadingNode(headingLevel, content)
        );

        // 5. 构建当前标题路径
        List<String> headingPath = context.headingStack.stream()
                .map(HeadingNode::title)
                .toList();

        // 6. 构建 DocumentBlock
        return DocumentBlock.builder()
                .type(DocumentBlockType.HEADING)
                .content(content)
                .headingLevel(headingLevel)
                .headingPath(headingPath)
                .metadata(parseBlockMetadata(node))
                .build();
    }

    // 解析普通段落
    private DocumentBlock parseParagraph(JsonNode node, ParseContext context) {
        String content = node.path("content").asText("");

        if (content.isBlank()) {
            return null;
        }
        // 获取当前标题路径
        List<String> headingPath = context.headingStack.stream()
                .map(HeadingNode::title)
                .toList();

        // 获取当前所属标题级别
        Integer headingLevel = context.headingStack.isEmpty() ? null : context.headingStack.peekLast().level();

        return DocumentBlock.builder()
                .type(DocumentBlockType.PARAGRAPH)
                .content(content)
                .headingLevel(headingLevel)
                .headingPath(headingPath)
                .metadata(parseBlockMetadata(node))
                .build();

    }

    // 解析列表
    private DocumentBlock parseList(JsonNode node, ParseContext context ) {
        JsonNode listItems = node.path("list items");
        if (!listItems.isArray() || listItems.isEmpty()) {
            return null;
        }
        List<String> contents = new ArrayList<>();
        for (JsonNode item : listItems) {
            String content = item.path("content").asText("");
            if (!content.isBlank()) {
                contents.add(content);
            }
        }
        if (contents.isEmpty()) {
            return null;
        }
        String content = String.join("\n", contents);
        List<String> headingPath = context.headingStack.stream()
                .map(HeadingNode::title)
                .toList();
        Integer headingLevel = context.headingStack.isEmpty() ? null : context.headingStack.peekLast().level();
        Map<String, Object> metadata = parseBlockMetadata(node);

        metadata.put("numberingStyle", node.path("numbering style").asText(null));

        metadata.put("numberOfListItems", node.path("number of list items").asInt());

        return DocumentBlock.builder()
                .type(DocumentBlockType.LIST)
                .content(content)
                .headingLevel(headingLevel)
                .headingPath(headingPath)
                .metadata(metadata)
                .build();
    }


    // 解析表格
    private DocumentBlock parseTable(JsonNode node, ParseContext context) {
        // ...
        return null;
    }

    // 提取公共元数据
    private Map<String, Object> parseBlockMetadata(JsonNode node) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sourceType", node.path("type").asText(null));
        putMetadata(metadata, "sourceId", node, "id");
        putMetadata(metadata, "pageNumber", node, "page number");
        putMetadata(metadata, "boundingBox", node, "bounding box");
        putMetadata(metadata, "pdfuaTag", node, "pdfua_tag");
        putMetadata(metadata, "semanticLevel", node, "level");
        putMetadata(metadata, "font", node, "font");
        putMetadata(metadata, "fontSize", node, "font size");
        putMetadata(metadata, "textColor", node, "text color");

        return metadata;
    }
    private void putMetadata(Map<String, Object> metadata, String key, JsonNode node, String jsonField) {
        JsonNode value = node.get(jsonField);
        if (value != null && !value.isNull()) {
            metadata.put(key, objectMapper.convertValue(value, Object.class));
        }
    }
}


