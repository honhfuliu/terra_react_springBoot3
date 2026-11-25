package com.ziheng.ai.infrastructure.vector.mapper;

import com.ziheng.ai.infrastructure.vector.domain.AiDocumentChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【ai_document_chunk(AI知识库文档分块表)】的数据库操作Mapper
* @createDate 2026-11-15 16:31:44
* @Entity generator.domain.AiDocumentChunk
*/
public interface AiDocumentChunkMapper extends BaseMapper<AiDocumentChunk> {

    /**
     * 根据文档ID删除所有分块
     */
    @Delete("""
        DELETE FROM ai_document_chunk
        WHERE document_id = #{documentId}
        """)
    int deleteByDocumentId(@Param("documentId") Long documentId);
}




