package com.ziheng.ai.infrastructure.knowledge.mapper;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

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

}




