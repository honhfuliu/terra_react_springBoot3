package com.ziheng.ai.infrastructure.vector.service;

import com.ziheng.ai.infrastructure.vector.domain.AiDocumentChunk;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【ai_document_chunk(AI知识库文档分块表)】的数据库操作Service
* @createDate 2026-11-15 16:31:44
*/
public interface AiDocumentChunkService extends IService<AiDocumentChunk> {

    /**
     * 测试插入：构造一条所有字段都有值的分块数据，写入vector库
     *
     * @return 插入后的分块（含数据库生成并回填的主键）
     */
    AiDocumentChunk insertTestChunk();

}
