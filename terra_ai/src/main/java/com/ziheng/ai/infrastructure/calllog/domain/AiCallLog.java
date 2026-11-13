package com.ziheng.ai.infrastructure.calllog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI调用日志表
 * @TableName ai_call_log
 */
@TableName(value ="ai_call_log")
@Data
public class AiCallLog {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调用用户ID
     */
    private Long userId;

    /**
     * 聊天会话ID
     */
    private Long sessionId;

    /**
     * AI回复消息ID
     */
    private Long messageId;

    /**
     * 供应商ID
     */
    private Long providerId;

    /**
     * 供应商名称快照
     */
    private String providerName;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型名称快照
     */
    private String modelName;

    /**
     * 调用类型 CHAT聊天 EMBEDDING向量化
     */
    private String callType;

    /**
     * 调用状态，取值见 AiCallStatusEnum：0进行中 1成功 2失败 3已取消
     */
    private String status;

    /**
     * 输入Token数量
     */
    private Integer promptTokens;

    /**
     * 输出Token数量
     */
    private Integer completionTokens;

    /**
     * 总Token数量
     */
    private Integer totalTokens;

    /**
     * 调用耗时 毫秒
     */
    private Long duration;

    /**
     * 异常信息
     */
    private String errorMessage;

    /**
     * 调用开始时间
     */
    private Date createTime;

    /**
     * 日志更新时间
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
        AiCallLog other = (AiCallLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getSessionId() == null ? other.getSessionId() == null : this.getSessionId().equals(other.getSessionId()))
            && (this.getMessageId() == null ? other.getMessageId() == null : this.getMessageId().equals(other.getMessageId()))
            && (this.getProviderId() == null ? other.getProviderId() == null : this.getProviderId().equals(other.getProviderId()))
            && (this.getProviderName() == null ? other.getProviderName() == null : this.getProviderName().equals(other.getProviderName()))
            && (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getModelName() == null ? other.getModelName() == null : this.getModelName().equals(other.getModelName()))
            && (this.getCallType() == null ? other.getCallType() == null : this.getCallType().equals(other.getCallType()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getPromptTokens() == null ? other.getPromptTokens() == null : this.getPromptTokens().equals(other.getPromptTokens()))
            && (this.getCompletionTokens() == null ? other.getCompletionTokens() == null : this.getCompletionTokens().equals(other.getCompletionTokens()))
            && (this.getTotalTokens() == null ? other.getTotalTokens() == null : this.getTotalTokens().equals(other.getTotalTokens()))
            && (this.getDuration() == null ? other.getDuration() == null : this.getDuration().equals(other.getDuration()))
            && (this.getErrorMessage() == null ? other.getErrorMessage() == null : this.getErrorMessage().equals(other.getErrorMessage()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getSessionId() == null) ? 0 : getSessionId().hashCode());
        result = prime * result + ((getMessageId() == null) ? 0 : getMessageId().hashCode());
        result = prime * result + ((getProviderId() == null) ? 0 : getProviderId().hashCode());
        result = prime * result + ((getProviderName() == null) ? 0 : getProviderName().hashCode());
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getModelName() == null) ? 0 : getModelName().hashCode());
        result = prime * result + ((getCallType() == null) ? 0 : getCallType().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getPromptTokens() == null) ? 0 : getPromptTokens().hashCode());
        result = prime * result + ((getCompletionTokens() == null) ? 0 : getCompletionTokens().hashCode());
        result = prime * result + ((getTotalTokens() == null) ? 0 : getTotalTokens().hashCode());
        result = prime * result + ((getDuration() == null) ? 0 : getDuration().hashCode());
        result = prime * result + ((getErrorMessage() == null) ? 0 : getErrorMessage().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", sessionId=").append(sessionId);
        sb.append(", messageId=").append(messageId);
        sb.append(", providerId=").append(providerId);
        sb.append(", providerName=").append(providerName);
        sb.append(", modelId=").append(modelId);
        sb.append(", modelName=").append(modelName);
        sb.append(", callType=").append(callType);
        sb.append(", status=").append(status);
        sb.append(", promptTokens=").append(promptTokens);
        sb.append(", completionTokens=").append(completionTokens);
        sb.append(", totalTokens=").append(totalTokens);
        sb.append(", duration=").append(duration);
        sb.append(", errorMessage=").append(errorMessage);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}