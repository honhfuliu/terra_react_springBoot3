package com.ziheng.ai.infrastructure.knowledge.chunk.impl;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingResult;
import com.knuddels.jtokkit.api.EncodingType;
import com.ziheng.ai.infrastructure.knowledge.chunk.TextChunk;
import com.ziheng.ai.infrastructure.knowledge.chunk.TextChunker;
import com.ziheng.ai.infrastructure.knowledge.parser.DocumentBlock;
import com.ziheng.ai.infrastructure.knowledge.parser.ParsedDocument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TokenTextChunker implements TextChunker {

    /**
     * Token 编码器
     * 与 TokenTextSplitter 使用相同的 CL100K_BASE 编码
     */
    private static final Encoding TOKEN_ENCODING = Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    @Override
    public List<TextChunk> split(ParsedDocument document, Integer chunkSize, Integer chunkOverlap) {
        // 参数校验
        validateParameters(chunkSize, chunkOverlap);

        if (document == null || document.getBlocks() == null || document.getBlocks().isEmpty()) {
            return List.of();
        }

        // 初始化切片列表
        List<TextChunk> chunks = new ArrayList<>();

        // 整个文档使用一个连续的 Chunk 编号
        int chunkIndex = 0;

        // 遍历 DocumentBlock 列表
        for (DocumentBlock block : document.getBlocks()) {
            if (block == null || block.getContent() == null || block.getContent().isBlank()) {
                continue;
            }
            // 执行 Token 切片
            List<String> splitContents = splitSafely(block.getContent(), chunkSize,  chunkOverlap);
            // 生成切片对象
            for (String content : splitContents) {
                if (content == null || content.isBlank()) {
                    continue;
                }
                TextChunk chunk = TextChunk.builder()
                        .content(content)
                        .chunkIndex(chunkIndex++)
                        .tokenCount(TOKEN_ENCODING.countTokensOrdinary(content))
                        .type(block.getType())
                        .headingPath(copyHeadingPath(block))
                        .headingLevel(block.getHeadingLevel())
                        .language(block.getLanguage())
                        .metadata(copyMetadata(block))
                        .build();

                chunks.add(chunk);
            }

        }

        return chunks;
    }



    /**
     * 安全切分文本，避免 Unicode 字符被切断
     *
     * @param text      原始文本
     * @param chunkSize 每个 Chunk 最大 Token 数
     * @param chunkOverlap 每个 Chunk 之间的重叠 Token 数
     * @return 切分后的文本列表
     */
    public List<String> splitSafely(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            String remaining = text.substring(start);
            // 剩余文本 Token 数量未超过限制，直接收尾
            if (TOKEN_ENCODING.countTokensOrdinary(remaining) <= chunkSize) {
                chunks.add(remaining);
                break;
            }

            // 通过原始字符串查找安全切片位置
            int safeEnd = findSafeEnd(remaining, chunkSize);

            if (safeEnd <= 0) {
                throw new IllegalStateException("无法找到安全切片位置");
            }

            // 只在原始字符串上截取，不进行 Token decode
            String safeChunk = remaining.substring(0, safeEnd);

            // 在安全范围内寻找更好的标点边界
            int splitPosition = findBestSplitBoundary(safeChunk);
            String finalChunk = safeChunk.substring(0, splitPosition);
            if (finalChunk.isEmpty()) {
                throw new IllegalStateException("切片结果为空");
            }
            int tokenCount = TOKEN_ENCODING.countTokensOrdinary(finalChunk);
            if (tokenCount > chunkSize) {
                throw new IllegalStateException("Chunk 超出 Token 数量限制");
            }

            chunks.add(finalChunk);
            // 计算下一个 Chunk 的起始位置
            int nextStart = findOverlapStart(
                    text,
                    start,
                    start + finalChunk.length(),
                    chunkOverlap
            );
            // 防止没有向前推进，造成死循环
            if (nextStart <= start) {
                throw new IllegalStateException("Chunk 切片位置没有向前推进");
            }
            // 从最终切片结束位置继续，确保不丢失文本
            start = nextStart;
        }

        return chunks;
    }

    /**
     * 查找下一个 Chunk 的起始位置。
     * 从当前 Chunk 的末尾向前保留指定数量的 Token，
     * 并确保切分位置位于完整 Unicode 码点边界。
     *
     * @param text 原始文本
     * @param start 当前 Chunk 起始位置
     * @param end 当前 Chunk 结束位置
     * @param overlapTokens 期望重叠的 Token 数量
     * @return 下一个 Chunk 在原始文本中的起始位置
     */
    private int findOverlapStart(String text, int start, int end, int overlapTokens) {
        if (overlapTokens <= 0) {
            return end;
        }

        String currentChunk = text.substring(start, end);

        int currentTokenCount =
                TOKEN_ENCODING.countTokensOrdinary(currentChunk);

        int targetOverlap = Math.min(overlapTokens, currentTokenCount - 1);

        if (targetOverlap <= 0) {
            return end;
        }

        int candidateStart = start;

        while (candidateStart < end) {
            int codePoint = text.codePointAt(candidateStart);
            candidateStart += Character.charCount(codePoint);

            String overlapContent = text.substring(candidateStart, end);
            int tokenCount = TOKEN_ENCODING.countTokensOrdinary(overlapContent);

            if (tokenCount <= targetOverlap) {
                break;
            }
        }

//        // 跳过重叠起点处连续的标点符号
//        while (candidateStart < end) {
//            int codePoint = text.codePointAt(candidateStart);
//
//            if (!isPunctuation(codePoint)) {
//                break;
//            }
//
//            candidateStart += Character.charCount(codePoint);
//        }

        return candidateStart;
    }

    private boolean isPunctuation(int codePoint) {
        int type = Character.getType(codePoint);

        return type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION;
    }

    /**
     * 找到安全的切片位置
     * @param text      原始文本
     * @param chunkSize 每个 Chunk 最大 Token 数
     * @return 安全的切片位置
     */
    private int findSafeEnd(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        EncodingResult result = TOKEN_ENCODING.encodeOrdinary(text, chunkSize);

        String decoded = TOKEN_ENCODING.decode(result.getTokens());

        int lastIndex = result.getLastProcessedCharacterIndex();
        if (lastIndex >= 0) {
            int candidateEnd = Math.min(lastIndex + 1, text.length());
            if (text.substring(0, candidateEnd).equals(decoded)) {
                return candidateEnd;
            }
        }
        // 索引语义无法验证时，退回解码结果长度
        if (text.startsWith(decoded)) {
            return decoded.length();
        }

        throw new IllegalStateException("Token 解码结果不是原文的完整前缀");

    }

    /**
     * 标点边界优先级
     */
    private static final List<String> SPLIT_PUNCTUATION_LEVELS = List.of("。！？", "；;", "，,", "、", "\n");

    /**
     * 标点回退范围比例
     * 只在安全切片末尾向前 20% 的范围内寻找标点
     */
    private static final double SPLIT_BOUNDARY_BACKTRACK_RATIO = 0.2;

    /**
     * 在安全切片内容中寻找最佳标点边界
     * @param text 安全切片内容
     * @return 最佳切分位置；没有合适标点时返回 text.length()
     */
    private int findBestSplitBoundary(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int minBoundary = (int) (
                text.length() * (1 - SPLIT_BOUNDARY_BACKTRACK_RATIO)
        );

        for (String punctuationGroup : SPLIT_PUNCTUATION_LEVELS) {
            for (int i = text.length(); i > minBoundary;) {
                int codePoint = text.codePointBefore(i);
                int charCount = Character.charCount(codePoint);

                if (punctuationGroup.indexOf(codePoint) >= 0) {
                    return i;
                }

                i -= charCount;
            }
        }

        return text.length();
    }


    /**
     * 参数校验
     */
    private void validateParameters(Integer chunkSize, Integer chunkOverlap) {

        if (chunkSize == null || chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize 必须大于 0");
        }

        if (chunkOverlap == null || chunkOverlap < 0) {
            throw new IllegalArgumentException("chunkOverlap 不能小于 0");
        }

        if (chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("chunkOverlap 必须小于 chunkSize");
        }
    }

    /**
     * 复制标题路径
     */
    private List<String> copyHeadingPath(DocumentBlock block) {
        if (block.getHeadingPath() == null) {
            return List.of();
        }
        return new ArrayList<>(block.getHeadingPath());
    }

    /**
     * 复制元数据
     */
    private Map<String, Object> copyMetadata(DocumentBlock block) {
        if (block.getMetadata() == null) {
            return new HashMap<>();
        }
        return new HashMap<>(block.getMetadata());
    }
}
