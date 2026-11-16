package com.ziheng.ai.infrastructure.vector.service;

import com.ziheng.ai.infrastructure.vector.domain.AiDocumentEmbedding;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.ai.infrastructure.vector.domain.dto.AiChunkSearchResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【ai_document_embedding(AI知识库文档向量表)】的数据库操作Service
 * @createDate 2026-11-15 16:31:44
 */
public interface AiDocumentEmbeddingService extends IService<AiDocumentEmbedding> {

    /**
     * 测试插入：构造一条除embedding外各字段都有值的向量记录，写入vector库
     *
     * @return 插入后的记录（含数据库生成并回填的主键）
     */
    AiDocumentEmbedding insertTestEmbedding();

    /**
     * 搜索相似向量
     *
     * @param knowledgeId     知识库ID
     * @param embeddingModelId  embedding模型ID
     * @param queryVector     查询向量
     * @param topK            返回最相似的TopK条记录
     * @return AiChunkSearchResult
     */
    List<AiChunkSearchResult> searchSimilar(Long knowledgeId, Long embeddingModelId, float[] queryVector, Integer topK);

}
