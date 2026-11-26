package com.ziheng.ai.infrastructure.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.ziheng.ai.infrastructure.knowledge.enums.AiDocumentProcessStatus;
import lombok.Data;

/**
 * AI知识库文档表
 * @TableName ai_knowledge_document
 */
@TableName(value ="ai_knowledge_document")
@Data
public class AiKnowledgeDocument {
    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属知识库ID
     */
    private Long knowledgeId;

    /**
     * 原始文件名称
     */
    private String fileName;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 文档页数
     */
    private Integer pageCount;

    /**
     * 切片数量
     */
    private Integer chunkCount;

    /**
     * 开始处理时间
     */
    private Date processStartTime;

    /**
     * 处理完成时间
     */
    private Date processEndTime;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 解析状态（PENDING待处理 PROCESSING处理中 SUCCESS成功 FAILED失败 RETRYING 重试中
     */
    private String parseStatus;

    /**
     * 向量化状态（PENDING待处理 PROCESSING处理中 SUCCESS成功 FAILED失败 RETRYING 重试中
     */
    private String embeddingStatus;

    /**
     * 切片状态（PENDING待处理 PROCESSING处理中 SUCCESS成功 FAILED失败 RETRYING 重试中）
     */
    private String chunkStatus;

    /**
     * 解析后的 Markdown 文件路径
     */
    private String parsedMdPath;

    /**
     * 解析后的 JSON 文件路径
     */
    private String parsedJsonPath;


    public static AiKnowledgeDocument copy(AiKnowledgeDocument source){
        AiKnowledgeDocument target = new AiKnowledgeDocument();
        target.setId(null);
        target.setKnowledgeId(source.getKnowledgeId());
        target.setFileName(source.getFileName());
        target.setFileExt(source.getFileExt());
        target.setFileSize(source.getFileSize());
        target.setFilePath(source.getFilePath());
        target.setFileMd5(source.getFileMd5());
        target.setMimeType(source.getMimeType());
        target.setPageCount(0);
        target.setChunkCount(0);
        target.setProcessStartTime(new Date());
        target.setProcessEndTime(null);
        target.setCreateBy(null);
        target.setCreateTime(new Date());
        target.setUpdateBy(null);
        target.setUpdateTime(new Date());
        target.setParseStatus(AiDocumentProcessStatus.PROCESSING.getCode());
        target.setEmbeddingStatus(AiDocumentProcessStatus.PENDING.getCode());
        target.setChunkStatus(AiDocumentProcessStatus.PENDING.getCode());
        target.setParsedMdPath(null);
        target.setParsedJsonPath(null);
        return target;
    }

}