
package com.ziheng.ai.infrastructure.knowledge.parser.impl;

import com.ziheng.ai.infrastructure.knowledge.parser.DocumentBlock;
import com.ziheng.ai.infrastructure.knowledge.parser.DocumentParser;
import com.ziheng.ai.infrastructure.knowledge.parser.ParsedDocument;
import com.ziheng.ai.infrastructure.knowledge.parser.enums.DocumentBlockType;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownDocumentParser implements DocumentParser {

    /**
     * Markdown 标题匹配
     */
    private static final Pattern HEADING_PATTERN = Pattern.compile(
            "^ {0,3}(#{1,6})(?:[ \\t]+(.*)|[ \\t]*)$"
    );

    /**
     * Markdown 代码围栏匹配
     */
    private static final Pattern FENCE_PATTERN = Pattern.compile(
            "^ {0,3}(`{3,}|~{3,})(.*)$"
    );

    /**
     * Markdown Reader 配置
     */
    private final MarkdownDocumentReaderConfig config;

    public MarkdownDocumentParser() {
        this.config = MarkdownDocumentReaderConfig.builder()
                .withIncludeCodeBlock(false) // 不解析代码块
                .withIncludeBlockquote(false)  // 不解析块引用
                .withHorizontalRuleCreateDocument(true)  // 解析水平线
                .build();
    }

    /**
     * Markdown 内容段
     */
    public record MarkdownContentSegment(String content, List<String> headingPath, Integer headingLevel) { }

    /**
     * 解析 Markdown 文件
     */
    @Override
    public ParsedDocument parse(String filePath) {

        // 1. 读取 Markdown 文件原始内容
        String markdown;

        try {
            markdown = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取 Markdown 文件失败：" + filePath, e);
        }
        System.out.println(
                "原始文本是否包含替换字符：" +
                        markdown.contains("\uFFFD")
        );
        // 2. 按标题层级拆分内容
        List<MarkdownContentSegment> segments = splitByHeading(markdown);

        // 3. 将每个片段交给 MarkdownDocumentReader 解析
        List<DocumentBlock> blocks = new ArrayList<>();

        for (MarkdownContentSegment segment : segments) {

            ByteArrayResource resource = new ByteArrayResource(segment.content().getBytes(StandardCharsets.UTF_8)) {
                @Override
                public String getFilename() {
                    return "segment.md";
                }
            };
            MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
            List<Document> documents = reader.get();

            // 4. 转换 Spring AI Document
            for (Document document : documents) {
                String content = document.getText();
                if (content == null || content.isBlank()) {
                    continue;
                }
                Map<String, Object> metadata = new HashMap<>(document.getMetadata());

                Object categoryValue = metadata.get("category");
                Object languageValue = metadata.get("lang");

                String category = categoryValue instanceof String ? (String) categoryValue : null;

                String language = languageValue instanceof String ? (String) languageValue : null;

                DocumentBlock block = DocumentBlock.builder()
                        .type(resolveBlockType(category))
                        .content(content)
                        .headingLevel(segment.headingLevel())
                        .language(language)
                        .headingPath(segment.headingPath())
                        .metadata(metadata)
                        .build();

                blocks.add(block);
            }
        }

        // 5. 组装解析结果
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileType", "markdown");
        metadata.put("filePath", filePath);
        metadata.put("blockCount", blocks.size());

        return ParsedDocument.builder()
                .blocks(blocks)
                .pageCount(null)
                .metadata(metadata)
                .build();
    }

    /**
     * 判断是否支持解析该文件类型
     */
    @Override
    public boolean supports(String fileExt) {
        if (fileExt == null || fileExt.isBlank()) {
            return false;
        }

        String ext = fileExt.toLowerCase().trim();

        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }

        return "md".equals(ext) || "markdown".equals(ext);
    }

    /**
     * 转换文档块类型
     */
    private DocumentBlockType resolveBlockType(String category) {
//        if (category == null || category.isBlank()) {
//            return DocumentBlockType.OTHER;
//        }
//        if ("code_block".equals(category)) {
//            return DocumentBlockType.CODE;
//        }
//        if ("blockquote".equals(category)) {
//            return DocumentBlockType.BLOCKQUOTE;
//        }
//        if (category.startsWith("header_")
//                || "paragraph".equals(category)) {
//            return DocumentBlockType.PARAGRAPH;
//        }
//        return DocumentBlockType.OTHER;
        if ("code_block".equals(category)) {
            return DocumentBlockType.CODE;
        }

        if ("blockquote".equals(category)) {
            return DocumentBlockType.BLOCKQUOTE;
        }

        if (category == null
                || category.isBlank()
                || category.startsWith("header_")
                || "paragraph".equals(category)
                || "code_inline".equals(category)) {
            return DocumentBlockType.PARAGRAPH;
        }

        return DocumentBlockType.OTHER;
    }


    /**
     * 根据 Markdown 标题切分文档内容
     */
    public List<MarkdownContentSegment> splitByHeading(String markdown) {

        List<MarkdownContentSegment> segments = new ArrayList<>();

        // 当前标题路径
        List<String> headingStack = new ArrayList<>();

        // 当前内容段
        StringBuilder currentContent = new StringBuilder();

        // 当前内容段对应的标题路径
        List<String> currentHeadingPath = List.of();

        // 当前内容段对应的标题级别
        Integer currentHeadingLevel = null;

        // 是否处于代码块内部
        boolean inCodeBlock = false;

        // 代码块围栏字符
        char fenceChar = 0;

        // 代码块围栏长度
        int fenceLength = 0;

        String[] lines = markdown.split("\\R", -1);

        for (String line : lines) {
            // 1. 处理代码块内部
            if (inCodeBlock) {
                if (isClosingFence(line, fenceChar, fenceLength)) {
                    inCodeBlock = false;
                    fenceChar = 0;
                    fenceLength = 0;
                }
                currentContent.append(line).append("\n");
                continue;
            }

            // 2. 判断代码块开始标记
            Matcher fenceMatcher = FENCE_PATTERN.matcher(line);
            if (fenceMatcher.matches()) {
                String fence = fenceMatcher.group(1);
                String info = fenceMatcher.group(2);
                // 反引号围栏的 info string 不能包含反引号
                if (fence.charAt(0) == '~' || !info.contains("`")) {
                    inCodeBlock = true;
                    fenceChar = fence.charAt(0);
                    fenceLength = fence.length();
                    currentContent.append(line).append("\n");
                    continue;
                }
            }

            // 3. 判断当前行是否为 ATX 标题
            Matcher headingMatcher = HEADING_PATTERN.matcher(line);

            if (headingMatcher.matches()) {
                // 先保存上一个内容段
                addSegment(segments, currentContent, currentHeadingPath, currentHeadingLevel);

                // 获取标题层级
                int headingLevel = headingMatcher.group(1).length();

                // 获取标题文本
                String heading = headingMatcher.group(2);

                if (heading == null) {
                    heading = "";
                }

                // 去除标题末尾的闭合 # 标记
                heading = heading.replaceFirst("[ \\t]+#+[ \\t]*$", "").trim();

                // 更新标题栈
                while (headingStack.size() >= headingLevel) {
                    headingStack.remove(headingStack.size() - 1);
                }

                // 空标题不入栈
                if (!heading.isBlank()) {
                    headingStack.add(heading);
                }

                // 保存当前标题路径快照
                currentHeadingPath = List.copyOf(headingStack);

                // 保存当前标题级别
                currentHeadingLevel = headingLevel;

                // 当前新内容段从标题行开始
                currentContent.setLength(0);
                currentContent.append(line).append("\n");

            } else {

                // 普通内容直接追加
                currentContent.append(line).append("\n");
            }
        }

        // 保存最后一个内容段
        addSegment(segments, currentContent, currentHeadingPath, currentHeadingLevel);
        return segments;
    }

    /**
     * 保存当前内容段
     */
    private void addSegment(List<MarkdownContentSegment> segments, StringBuilder content,
            List<String> headingPath, Integer headingLevel) {
        String text = content.toString();
        if (!text.isBlank()) {
            segments.add(new MarkdownContentSegment(text, List.copyOf(headingPath), headingLevel));
        }
        content.setLength(0);
    }

    /**
     * 判断是否为代码块结束围栏
     */
    private boolean isClosingFence(String line, char fenceChar, int fenceLength) {
        String regex = "^ {0,3}" + Pattern.quote(String.valueOf(fenceChar)) + "{" + fenceLength + ",}[ \\t]*$";
        return Pattern.matches(regex, line);
    }
}
