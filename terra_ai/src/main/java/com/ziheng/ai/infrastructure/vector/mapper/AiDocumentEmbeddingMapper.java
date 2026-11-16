package com.ziheng.ai.infrastructure.vector.mapper;

import com.ziheng.ai.infrastructure.vector.domain.AiDocumentEmbedding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziheng.ai.infrastructure.vector.domain.dto.AiChunkSearchResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_document_embedding(AI知识库文档向量表)】的数据库操作Mapper
* @createDate 2026-11-15 16:31:44
* @Entity generator.domain.AiDocumentEmbedding
*/
public interface AiDocumentEmbeddingMapper extends BaseMapper<AiDocumentEmbedding> {

    /**
     * 根据知识库ID、嵌入模型ID和查询向量，搜索相似的文档向量
     *
     * @param knowledgeId     知识库ID
     * @param embeddingModelId 嵌入模型ID
     * @param queryVector     查询向量
     * @param topK            返回的最相似向量数量
     * @return AiChunkSearchResult
     */
    List<AiChunkSearchResult> searchSimilar(@Param("knowledgeId") Long knowledgeId, @Param("embeddingModelId") Long embeddingModelId, @Param("queryVector") float[] queryVector, @Param("topK") Integer topK);

}




