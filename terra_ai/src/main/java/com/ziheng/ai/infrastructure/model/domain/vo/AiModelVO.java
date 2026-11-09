package com.ziheng.ai.infrastructure.model.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型列表视图对象
 */
@Data
public class AiModelVO {

    /**
     * 主键（操作列使用）
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型编码
     */
    private String modelCode;

    /**
     * 所属提供商名称
     */
    private String providerName;

    /**
     * 模型类型 CHAT/EMBEDDING/RERANK
     */
    private String modelType;

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
     * 状态 0停用 1启用
     */
    private String enabled;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否默认 0否 1是
     */
    private String isDefault;
}
