package com.ziheng.ai.infrastructure.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
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
     * 处理状态（0待处理 1处理中 2成功 3失败）
     */
    private String status;

    /**
     * 处理失败原因
     */
    private String errorMessage;

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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AiKnowledgeDocument other = (AiKnowledgeDocument) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getKnowledgeId() == null ? other.getKnowledgeId() == null : this.getKnowledgeId().equals(other.getKnowledgeId()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileExt() == null ? other.getFileExt() == null : this.getFileExt().equals(other.getFileExt()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getFilePath() == null ? other.getFilePath() == null : this.getFilePath().equals(other.getFilePath()))
            && (this.getFileMd5() == null ? other.getFileMd5() == null : this.getFileMd5().equals(other.getFileMd5()))
            && (this.getMimeType() == null ? other.getMimeType() == null : this.getMimeType().equals(other.getMimeType()))
            && (this.getPageCount() == null ? other.getPageCount() == null : this.getPageCount().equals(other.getPageCount()))
            && (this.getChunkCount() == null ? other.getChunkCount() == null : this.getChunkCount().equals(other.getChunkCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getErrorMessage() == null ? other.getErrorMessage() == null : this.getErrorMessage().equals(other.getErrorMessage()))
            && (this.getProcessStartTime() == null ? other.getProcessStartTime() == null : this.getProcessStartTime().equals(other.getProcessStartTime()))
            && (this.getProcessEndTime() == null ? other.getProcessEndTime() == null : this.getProcessEndTime().equals(other.getProcessEndTime()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getKnowledgeId() == null) ? 0 : getKnowledgeId().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileExt() == null) ? 0 : getFileExt().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getFilePath() == null) ? 0 : getFilePath().hashCode());
        result = prime * result + ((getFileMd5() == null) ? 0 : getFileMd5().hashCode());
        result = prime * result + ((getMimeType() == null) ? 0 : getMimeType().hashCode());
        result = prime * result + ((getPageCount() == null) ? 0 : getPageCount().hashCode());
        result = prime * result + ((getChunkCount() == null) ? 0 : getChunkCount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getErrorMessage() == null) ? 0 : getErrorMessage().hashCode());
        result = prime * result + ((getProcessStartTime() == null) ? 0 : getProcessStartTime().hashCode());
        result = prime * result + ((getProcessEndTime() == null) ? 0 : getProcessEndTime().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", knowledgeId=").append(knowledgeId);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileExt=").append(fileExt);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", filePath=").append(filePath);
        sb.append(", fileMd5=").append(fileMd5);
        sb.append(", mimeType=").append(mimeType);
        sb.append(", pageCount=").append(pageCount);
        sb.append(", chunkCount=").append(chunkCount);
        sb.append(", status=").append(status);
        sb.append(", errorMessage=").append(errorMessage);
        sb.append(", processStartTime=").append(processStartTime);
        sb.append(", processEndTime=").append(processEndTime);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}