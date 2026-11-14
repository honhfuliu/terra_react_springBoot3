package com.ziheng.ai.infrastructure.knowledge.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI知识库表
 * @TableName ai_knowledge
 */
@TableName(value ="ai_knowledge")
@Data
public class AiKnowledge {
    /**
     * 知识库ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * Embedding模型ID
     */
    private Long embeddingModelId;

    /**
     * Embedding模型名称快照
     */
    private String embeddingModelName;

    /**
     * 向量维度
     */
    private Integer embeddingDimension;

    /**
     * 切片长度
     */
    private Integer chunkSize;

    /**
     * 切片重叠长度
     */
    private Integer chunkOverlap;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 切片数量
     */
    private Long chunkCount;

    /**
     * 状态（0停用 1启用）
     */
    private String status;

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
     * 备注
     */
    private String remark;

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
        AiKnowledge other = (AiKnowledge) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getEmbeddingModelId() == null ? other.getEmbeddingModelId() == null : this.getEmbeddingModelId().equals(other.getEmbeddingModelId()))
            && (this.getEmbeddingModelName() == null ? other.getEmbeddingModelName() == null : this.getEmbeddingModelName().equals(other.getEmbeddingModelName()))
            && (this.getEmbeddingDimension() == null ? other.getEmbeddingDimension() == null : this.getEmbeddingDimension().equals(other.getEmbeddingDimension()))
            && (this.getChunkSize() == null ? other.getChunkSize() == null : this.getChunkSize().equals(other.getChunkSize()))
            && (this.getChunkOverlap() == null ? other.getChunkOverlap() == null : this.getChunkOverlap().equals(other.getChunkOverlap()))
            && (this.getDocumentCount() == null ? other.getDocumentCount() == null : this.getDocumentCount().equals(other.getDocumentCount()))
            && (this.getChunkCount() == null ? other.getChunkCount() == null : this.getChunkCount().equals(other.getChunkCount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getEmbeddingModelId() == null) ? 0 : getEmbeddingModelId().hashCode());
        result = prime * result + ((getEmbeddingModelName() == null) ? 0 : getEmbeddingModelName().hashCode());
        result = prime * result + ((getEmbeddingDimension() == null) ? 0 : getEmbeddingDimension().hashCode());
        result = prime * result + ((getChunkSize() == null) ? 0 : getChunkSize().hashCode());
        result = prime * result + ((getChunkOverlap() == null) ? 0 : getChunkOverlap().hashCode());
        result = prime * result + ((getDocumentCount() == null) ? 0 : getDocumentCount().hashCode());
        result = prime * result + ((getChunkCount() == null) ? 0 : getChunkCount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", description=").append(description);
        sb.append(", embeddingModelId=").append(embeddingModelId);
        sb.append(", embeddingModelName=").append(embeddingModelName);
        sb.append(", embeddingDimension=").append(embeddingDimension);
        sb.append(", chunkSize=").append(chunkSize);
        sb.append(", chunkOverlap=").append(chunkOverlap);
        sb.append(", documentCount=").append(documentCount);
        sb.append(", chunkCount=").append(chunkCount);
        sb.append(", status=").append(status);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", remark=").append(remark);
        sb.append("]");
        return sb.toString();
    }
}