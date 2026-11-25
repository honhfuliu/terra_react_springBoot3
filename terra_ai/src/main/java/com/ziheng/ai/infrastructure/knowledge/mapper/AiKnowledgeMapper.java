package com.ziheng.ai.infrastructure.knowledge.mapper;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author Administrator
* @description 针对表【ai_knowledge(AI知识库表)】的数据库操作Mapper
* @createDate 2026-11-14 21:07:01
* @Entity generator.domain.AiKnowledge
*/
public interface AiKnowledgeMapper extends BaseMapper<AiKnowledge> {

    /**
     * 统计所有知识库的文档总数
     */
    @Select("SELECT COALESCE(SUM(document_count), 0) FROM ai_knowledge")
    Long sumDocumentCount();

    /**
     * 统计所有知识库的切片总数
     */
    @Select("SELECT COALESCE(SUM(chunk_count), 0) FROM ai_knowledge")
    Long sumChunkCount();


    /**
     * 增加知识库文档数量
     */
    @Update("""
    UPDATE ai_knowledge
    SET document_count = document_count + 1
    WHERE id = #{knowledgeId}
    """)
    int incrementDocumentCount(@Param("knowledgeId") Long knowledgeId);

    /**
     * 减少知识库文档数量（不低于0）
     */
    @Update("""
    UPDATE ai_knowledge
    SET document_count = CASE WHEN document_count > 0 THEN document_count - 1 ELSE 0 END
    WHERE id = #{knowledgeId}
    """)
    int decrementDocumentCount(@Param("knowledgeId") Long knowledgeId);

    /**
     * 增加知识库切片数量
     */
    @Update("""
    UPDATE ai_knowledge
    SET chunk_count = chunk_count + #{chunkCount}
    WHERE id = #{knowledgeId}
    """)
    int incrementChunkCount(@Param("knowledgeId") Long knowledgeId, @Param("chunkCount") int chunkCount);

    /**
     * 减少知识库切片数量（不低于0）
     */
    @Update("""
    UPDATE ai_knowledge
    SET chunk_count = CASE WHEN chunk_count > #{chunkCount} THEN chunk_count - #{chunkCount} ELSE 0 END
    WHERE id = #{knowledgeId}
    """)
    int decrementChunkCount(@Param("knowledgeId") Long knowledgeId, @Param("chunkCount") int chunkCount);

}




