package com.ziheng.ai.infrastructure.knowledge.mapper;

import com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentCleanupTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_knowledge_document_cleanup_task(知识库文档资源清理任务表)】的数据库操作Mapper
* @createDate 2026-11-25 14:47:54
* @Entity com.ziheng.ai.infrastructure.knowledge.domain.AiKnowledgeDocumentCleanupTask
*/
public interface AiKnowledgeDocumentCleanupTaskMapper extends BaseMapper<AiKnowledgeDocumentCleanupTask> {

    /**
     * 查询并锁定待执行的清理任务
     */
    @Select("""
        SELECT *
        FROM ai_knowledge_document_cleanup_task
        WHERE status IN ('PENDING', 'RETRYING')
        ORDER BY create_time ASC
        LIMIT #{limit}
        FOR UPDATE SKIP LOCKED
        """)
    List<AiKnowledgeDocumentCleanupTask> selectPendingTasksForUpdate(@Param("limit") int limit);
}




