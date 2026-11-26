package com.ziheng.ai.infrastructure.vector.service;

import com.ziheng.ai.infrastructure.vector.domain.AiDocumentChunk;
import com.ziheng.ai.infrastructure.vector.domain.dto.AiDocumentChunkQuery;
import com.ziheng.ai.infrastructure.vector.domain.vo.AiDocumentChunkVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.DoclingChunk;
import com.ziheng.common.core.domain.PageResult;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_document_chunk(AI知识库文档分块表)】的数据库操作Service
* @createDate 2026-11-15 16:31:44
*/
public interface AiDocumentChunkService extends IService<AiDocumentChunk> {


    /**
     * 幂等保存文档分块：先删除该文档已有分块，再批量写入新分块
     *
     * @param knowledgeId 知识库ID
     * @param documentId  文档ID
     * @param chunks      Docling 返回的分块结果
     */
    void saveChunks(Long knowledgeId, Long documentId, List<DoclingChunk> chunks);

    /**
     * 按文档ID和内容模糊分页查询分块
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AiDocumentChunkVO> page(AiDocumentChunkQuery query);


    /**
     * 删除文档对应的 Chunk 和 Embedding
     */
    void deleteByDocumentId(Long documentId);

    /**
     * 查询文档全部 Chunk
     *
     * @param documentId 文档ID
     * @return 文档分块列表
     */
    List<AiDocumentChunk> listByDocumentId(Long documentId);
}
