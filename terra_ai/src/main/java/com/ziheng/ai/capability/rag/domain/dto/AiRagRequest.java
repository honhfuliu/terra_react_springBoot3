package com.ziheng.ai.capability.rag.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiRagRequest {

    /**
     * 知识库 ID
     */
    @NotNull(message = "知识库 ID 不能为空")
    private Long knowledgeId;

    /**
     * 聊天模型 ID
     */
    @NotNull(message = "聊天模型 ID 不能为空")
    private Long modelId;

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 相似度阈值（0.1 ~ 1）
     */
    @DecimalMin(value = "0.1", message = "相似度阈值不能小于 0.1")
    @DecimalMax(value = "1", message = "相似度阈值不能大于 1")
    private Double similarityThreshold;

    /**
     * topK（不传或传 null 时使用默认值）
     */
    private Integer topK = 10;


}
