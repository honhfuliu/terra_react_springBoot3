package com.ziheng.ai.infrastructure.vector.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingChunk;
import com.ziheng.ai.infrastructure.vector.domain.AiDocumentChunk;
import com.ziheng.ai.infrastructure.vector.domain.dto.AiDocumentChunkQuery;
import com.ziheng.ai.infrastructure.vector.domain.vo.AiDocumentChunkVO;
import com.ziheng.ai.infrastructure.vector.mapper.AiDocumentChunkMapper;
import com.ziheng.ai.infrastructure.vector.mapper.AiDocumentEmbeddingMapper;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author Administrator
* @description 针对表【ai_document_chunk(AI知识库文档分块表)】的数据库操作Service实现
* @createDate 2026-11-15 16:31:44
*/
@Service
@DS("vector")
public class AiDocumentChunkServiceImpl extends ServiceImpl<AiDocumentChunkMapper, AiDocumentChunk>
    implements AiDocumentChunkService{

    private final ObjectMapper objectMapper;
    private final AiDocumentEmbeddingMapper embeddingMapper;

    public AiDocumentChunkServiceImpl(ObjectMapper objectMapper, AiDocumentEmbeddingMapper embeddingMapper) {
        this.objectMapper = objectMapper;
        this.embeddingMapper = embeddingMapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChunks(Long knowledgeId, Long documentId, List<DoclingChunk> chunks) {
        // 先清理旧分块，保证任务重试时不会重复写入
        remove(Wrappers.<AiDocumentChunk>lambdaQuery()
                .eq(AiDocumentChunk::getDocumentId, documentId));

        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<AiDocumentChunk> entities = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            DoclingChunk chunk = chunks.get(i);
            AiDocumentChunk entity = new AiDocumentChunk();
            entity.setKnowledgeId(knowledgeId);
            entity.setDocumentId(documentId);
            entity.setChunkIndex(chunk.getChunkIndex() == null ? i : chunk.getChunkIndex());
            entity.setContent(chunk.getText());
            entity.setTokenCount(chunk.getNumTokens());
            entity.setMetadata(buildMetadata(chunk));
            entity.setCreateTime(now);
            entities.add(entity);
        }
        saveBatch(entities);
    }

    @Override
    public PageResult<AiDocumentChunkVO> page(AiDocumentChunkQuery query) {
        if (query.getDocumentId() == null) {
            throw new BusinessException("文档ID不能为空");
        }
        Page<AiDocumentChunk> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiDocumentChunk> wrapper = new LambdaQueryWrapper<AiDocumentChunk>()
                .eq(AiDocumentChunk::getDocumentId, query.getDocumentId())
                .like(StringUtils.hasText(query.getContent()), AiDocumentChunk::getContent, query.getContent())
                .orderByAsc(AiDocumentChunk::getChunkIndex);

        Page<AiDocumentChunk> result = page(page, wrapper);
        List<AiDocumentChunkVO> rows = new ArrayList<>(result.getRecords().size());
        for (AiDocumentChunk chunk : result.getRecords()) {
            AiDocumentChunkVO vo = new AiDocumentChunkVO();
            vo.setId(chunk.getId());
            vo.setChunkIndex(chunk.getChunkIndex());
            vo.setContent(chunk.getContent());
            vo.setTokenCount(chunk.getTokenCount());

            Map<String, Object> metadata = chunk.getMetadata();
            if (metadata != null && metadata.get("pageNumbers") instanceof List<?> pageNumbers) {
                vo.setPageNumbers(pageNumbers.stream()
                        .map(number -> number instanceof Number value ? value.intValue() : null)
                        .filter(Objects::nonNull)
                        .toList());
            }
            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 根据文档ID删除所有分块 和向量数据
     * @param documentId 文档ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDocumentId(Long documentId) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }

        // 1. 删除文档对应的所有 Embedding
        embeddingMapper.deleteByDocumentId(documentId);

        // 2. 删除文档对应的所有 Chunk
        baseMapper.deleteByDocumentId(documentId);
    }


    /**
     * 按约定格式组织 jsonb 元数据
     */
    private Map<String, Object> buildMetadata(DoclingChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();
        JsonNode source = chunk.getMetadata();
        JsonNode origin = source == null ? null : source.path("origin");

        String originFileName = origin == null || origin.isMissingNode()
                ? null : origin.path("filename").asText(null);
        metadata.put("filename", originFileName == null ? chunk.getFilename() : originFileName);
        metadata.put("headings", chunk.getHeadings() == null ? List.of() : chunk.getHeadings());
        metadata.put("captions", chunk.getCaptions() == null ? List.of() : chunk.getCaptions());
        metadata.put("docItems", chunk.getDocItems() == null ? List.of() : chunk.getDocItems());
        metadata.put("pageNumbers", chunk.getPageNumbers() == null ? List.of() : chunk.getPageNumbers());
        metadata.put("rawText", chunk.getRawText());
        metadata.put("origin", origin == null || origin.isMissingNode()
                ? null : objectMapper.convertValue(origin, new TypeReference<Map<String, Object>>() {}));
        metadata.put("hasImage", source != null && !source.path("has_image").isMissingNode()
                && source.path("has_image").asBoolean(false));
        return metadata;
    }

}
