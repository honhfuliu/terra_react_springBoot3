package com.ziheng.ai.infrastructure.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * AI模型配置表
 * @TableName ai_model
 */
@TableName(value ="ai_model")
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
        AiModel other = (AiModel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getProviderId() == null ? other.getProviderId() == null : this.getProviderId().equals(other.getProviderId()))
            && (this.getModelCode() == null ? other.getModelCode() == null : this.getModelCode().equals(other.getModelCode()))
            && (this.getModelName() == null ? other.getModelName() == null : this.getModelName().equals(other.getModelName()))
            && (this.getModelType() == null ? other.getModelType() == null : this.getModelType().equals(other.getModelType()))
            && (this.getEnabled() == null ? other.getEnabled() == null : this.getEnabled().equals(other.getEnabled()))
            && (this.getIsDefault() == null ? other.getIsDefault() == null : this.getIsDefault().equals(other.getIsDefault()))
            && (this.getTemperature() == null ? other.getTemperature() == null : this.getTemperature().equals(other.getTemperature()))
            && (this.getMaxTokens() == null ? other.getMaxTokens() == null : this.getMaxTokens().equals(other.getMaxTokens()))
            && (this.getContextWindow() == null ? other.getContextWindow() == null : this.getContextWindow().equals(other.getContextWindow()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
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
        result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
        result = prime * result + ((getModelCode() == null) ? 0 : getModelCode().hashCode());
        result = prime * result + ((getModelName() == null) ? 0 : getModelName().hashCode());
        result = prime * result + ((getModelType() == null) ? 0 : getModelType().hashCode());
        result = prime * result + ((getEnabled() == null) ? 0 : getEnabled().hashCode());
        result = prime * result + ((getIsDefault() == null) ? 0 : getIsDefault().hashCode());
        result = prime * result + ((getTemperature() == null) ? 0 : getTemperature().hashCode());
        result = prime * result + ((getMaxTokens() == null) ? 0 : getMaxTokens().hashCode());
        result = prime * result + ((getContextWindow() == null) ? 0 : getContextWindow().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
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
        sb.append(", providerId=").append(providerId);
        sb.append(", modelCode=").append(modelCode);
        sb.append(", modelName=").append(modelName);
        sb.append(", modelType=").append(modelType);
        sb.append(", enabled=").append(enabled);
        sb.append(", isDefault=").append(isDefault);
        sb.append(", temperature=").append(temperature);
        sb.append(", maxTokens=").append(maxTokens);
        sb.append(", contextWindow=").append(contextWindow);
        sb.append(", remark=").append(remark);
        sb.append(", createBy=").append(createBy);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}