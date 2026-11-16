package com.ziheng.ai.infrastructure.knowledge.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI知识库新增/修改参数
 */
@Data
public class AiKnowledgeSaveDTO {

    /**
     * 主键（修改时必传，新增时为空）
     */
    private Long id;

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
    @Size(max = 100, message = "知识库名称长度不能超过100个字符")
    private String name;

    /**
     * 知识库描述
     */
    @Size(max = 500, message = "知识库描述长度不能超过500个字符")
    private String description;

    /**
     * Embedding模型ID
     */
    @NotNull(message = "Embedding模型不能为空")
    private Long embeddingModelId;

    /**
     * Embedding模型名称快照（为空时按模型ID自动补全）
     */
    @Size(max = 100, message = "Embedding模型名称长度不能超过100个字符")
    private String embeddingModelName;

    /**
     * Embedding向量维度（为空时按模型默认维度补全）
     */
    @Min(value = 1, message = "向量维度必须大于0")
    private Integer embeddingDimension;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    /**
     * 切片长度，为空时新增默认500
     */
    @Min(value = 1, message = "切片长度必须大于0")
    private Integer chunkSize;

    /**
     * 切片重叠长度，为空时新增默认0
     */
    @Min(value = 0, message = "切片重叠长度不能小于0")
    private Integer chunkOverlap;

    /**
     * 状态（0停用 1启用），为空时新增默认启用
     */
    private String status;
}
