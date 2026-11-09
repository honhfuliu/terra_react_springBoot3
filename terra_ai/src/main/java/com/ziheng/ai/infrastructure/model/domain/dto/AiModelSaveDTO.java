package com.ziheng.ai.infrastructure.model.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型新增/修改参数
 */
@Data
public class AiModelSaveDTO {

    /**
     * 主键（修改时必传，新增时为空）
     */
    private Long id;

    /**
     * 所属提供商ID
     */
    @NotNull(message = "所属提供商不能为空")
    private Long providerId;

    /**
     * 模型类型 CHAT/EMBEDDING/RERANK
     */
    @NotBlank(message = "模型类型不能为空")
    private String modelType;

    /**
     * 模型编码
     */
    @NotBlank(message = "模型编码不能为空")
    private String modelCode;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    /**
     * 默认温度
     */
    private BigDecimal temperature;

    /**
     * 最大输出Token
     */
    private Integer maxTokens;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;

    /**
     * 备注
     */
    private String remark;

    /**
     * 启用状态 0停用 1启用
     */
    private String enabled;

    /**
     * 设为默认模型 0否 1是（传1时先取消原有默认模型；修改时传0可取消默认）
     */
    private String isDefault;
}
