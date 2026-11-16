package com.ziheng.ai.infrastructure.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.ziheng.ai.infrastructure.model.typehandler.IntegerListJsonTypeHandler;
import lombok.Data;

/**
 * AI模型配置表
 * @TableName ai_model
 */
@TableName(value = "ai_model", autoResultMap = true)
@Data
public class AiModel {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属供应商ID
     */
    private Long providerId;

    /**
     * 模型编码
     */
    private String modelCode;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型类型 CHAT/EMBEDDING/RERANK
     */
    private String modelType;

    /**
     * 是否启用 0停用 1启用
     */
    private String enabled;

    /**
     * 是否默认模型 0否 1是
     */
    private String isDefault;

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
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private Long updateBy;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 默认Embedding维度
     */
    private Integer defaultEmbeddingDimension;
    /**
     * 是否可配置维度 0否 1是
     */
    private String dimensionConfigurable;

    /**
     * 支持的Embedding维度
     */
    @TableField(typeHandler = IntegerListJsonTypeHandler.class)
    private List<Integer> supportedDimensions;
}