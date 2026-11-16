package com.ziheng.ai.infrastructure.vector.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.infrastructure.vector.domain.AiDocumentChunk;
import com.ziheng.ai.infrastructure.vector.mapper.AiDocumentChunkMapper;
import com.ziheng.ai.infrastructure.vector.service.AiDocumentChunkService;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【ai_document_chunk(AI知识库文档分块表)】的数据库操作Service实现
* @createDate 2026-11-15 16:31:44
*/
@Service
@DS("vector")
public class AiDocumentChunkServiceImpl extends ServiceImpl<AiDocumentChunkMapper, AiDocumentChunk>
    implements AiDocumentChunkService{

    /**
     * 测试插入：每个字段都赋值（id为自增主键，由数据库生成后回填）
     */
    @Override
    public AiDocumentChunk insertTestChunk() {
        List<String> strings = List.of("Spring AI 可以集成不同的大语言模型。", "Java 是一种面向对象的编程语言。",
                "PostgreSQL 是一种开源关系型数据库。", "今天天气晴朗，适合外出散步。");
        for (int i = 0; i < strings.size(); i++) {
            AiDocumentChunk chunk = new AiDocumentChunk();
            chunk.setKnowledgeId(1L);
            chunk.setDocumentId(1L);
            chunk.setChunkIndex(i +1);
            chunk.setContent(strings.get(i));
            chunk.setTokenCount(20);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("page", 1);
            metadata.put("title", "测试文档");
            metadata.put("section", "第一章");
            metadata.put("position", "0-20");
            chunk.setMetadata(metadata);

            chunk.setCreateTime(LocalDateTime.now());
            save(chunk);
        }

        // insert后MyBatis-Plus会将自增主键回填，此时实体所有字段均有值
        return null;
    }

}
