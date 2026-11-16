package com.ziheng.ai.infrastructure.vector.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;

import com.ziheng.ai.infrastructure.vector.typehandler.PgVectorTypeHandler;
import lombok.Data;

/**
 * AI知识库文档向量表
 * @TableName ai_document_embedding
 */
@TableName(value ="ai_document_embedding")
@Data
public class AiDocumentEmbedding {
    /**
     * 向量记录主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联Chunk ID
     */
    private Long chunkId;

    /**
     * 知识库ID（冗余字段，用于向量检索过滤）
     */
    private Long knowledgeId;

    /**
     * Embedding模型ID（关联MySQL）
     */
    private Long embeddingModelId;

    /**
     * Embedding维度
     */
    private Integer embeddingDimension;

    /**
     * Embedding向量
     */
    @TableField(typeHandler = PgVectorTypeHandler.class)
    private float[] embedding;

    /**
     * 向量状态：0待生成，1生成成功，2生成失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}