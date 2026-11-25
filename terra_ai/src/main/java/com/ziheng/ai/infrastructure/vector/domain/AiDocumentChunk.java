package com.ziheng.ai.infrastructure.vector.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Map;

import com.ziheng.ai.infrastructure.vector.typehandler.JsonbTypeHandler;
import lombok.Data;

/**
 * AI知识库文档分块表
 * @TableName ai_document_chunk
 */
@TableName(value ="ai_document_chunk", autoResultMap = true)
@Data
public class AiDocumentChunk {
    /**
     * Chunk主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID（关联MySQL）
     */
    private Long knowledgeId;

    /**
     * 知识库文档ID（关联MySQL）
     */
    private Long documentId;

    /**
     * 文档内分块顺序，从0开始
     */
    private Integer chunkIndex;

    /**
     * 分块文本内容
     */
    private String content;

    /**
     * 分块Token数量
     */
    private Integer tokenCount;

    /**
     * 分块扩展元数据，例如页码、标题、位置信息
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}