package com.ziheng.system.sysoperationlog.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 系统操作审计日志
 * @TableName sys_operation_log
 */
@TableName(value ="sys_operation_log")
@Data
public class SysOperationLog {
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作账号
     */
    private String username;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 操作对象类型
     */
    private String targetType;

    /**
     * 操作对象ID
     */
    private String targetId;

    /**
     * 操作对象名称
     */
    private String targetName;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 操作位置
     */
    private String requestLocation;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 操作前数据
     */
    private String beforeData;

    /**
     * 操作后数据
     */
    private String afterData;

    /**
     * 操作状态：0失败，1成功
     */
    private String status;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 执行耗时，单位毫秒
     */
    private Long costTime;

    /**
     * 操作时间
     */
    private Date operationTime;

    /**
     * 创建时间
     */
    private Date createTime;

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
        SysOperationLog other = (SysOperationLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getModule() == null ? other.getModule() == null : this.getModule().equals(other.getModule()))
            && (this.getOperation() == null ? other.getOperation() == null : this.getOperation().equals(other.getOperation()))
            && (this.getTargetType() == null ? other.getTargetType() == null : this.getTargetType().equals(other.getTargetType()))
            && (this.getTargetId() == null ? other.getTargetId() == null : this.getTargetId().equals(other.getTargetId()))
            && (this.getTargetName() == null ? other.getTargetName() == null : this.getTargetName().equals(other.getTargetName()))
            && (this.getRequestMethod() == null ? other.getRequestMethod() == null : this.getRequestMethod().equals(other.getRequestMethod()))
            && (this.getRequestUrl() == null ? other.getRequestUrl() == null : this.getRequestUrl().equals(other.getRequestUrl()))
            && (this.getRequestIp() == null ? other.getRequestIp() == null : this.getRequestIp().equals(other.getRequestIp()))
            && (this.getRequestLocation() == null ? other.getRequestLocation() == null : this.getRequestLocation().equals(other.getRequestLocation()))
            && (this.getUserAgent() == null ? other.getUserAgent() == null : this.getUserAgent().equals(other.getUserAgent()))
            && (this.getRequestParams() == null ? other.getRequestParams() == null : this.getRequestParams().equals(other.getRequestParams()))
            && (this.getBeforeData() == null ? other.getBeforeData() == null : this.getBeforeData().equals(other.getBeforeData()))
            && (this.getAfterData() == null ? other.getAfterData() == null : this.getAfterData().equals(other.getAfterData()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getErrorMessage() == null ? other.getErrorMessage() == null : this.getErrorMessage().equals(other.getErrorMessage()))
            && (this.getCostTime() == null ? other.getCostTime() == null : this.getCostTime().equals(other.getCostTime()))
            && (this.getOperationTime() == null ? other.getOperationTime() == null : this.getOperationTime().equals(other.getOperationTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getModule() == null) ? 0 : getModule().hashCode());
        result = prime * result + ((getOperation() == null) ? 0 : getOperation().hashCode());
        result = prime * result + ((getTargetType() == null) ? 0 : getTargetType().hashCode());
        result = prime * result + ((getTargetId() == null) ? 0 : getTargetId().hashCode());
        result = prime * result + ((getTargetName() == null) ? 0 : getTargetName().hashCode());
        result = prime * result + ((getRequestMethod() == null) ? 0 : getRequestMethod().hashCode());
        result = prime * result + ((getRequestUrl() == null) ? 0 : getRequestUrl().hashCode());
        result = prime * result + ((getRequestIp() == null) ? 0 : getRequestIp().hashCode());
        result = prime * result + ((getRequestLocation() == null) ? 0 : getRequestLocation().hashCode());
        result = prime * result + ((getUserAgent() == null) ? 0 : getUserAgent().hashCode());
        result = prime * result + ((getRequestParams() == null) ? 0 : getRequestParams().hashCode());
        result = prime * result + ((getBeforeData() == null) ? 0 : getBeforeData().hashCode());
        result = prime * result + ((getAfterData() == null) ? 0 : getAfterData().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getErrorMessage() == null) ? 0 : getErrorMessage().hashCode());
        result = prime * result + ((getCostTime() == null) ? 0 : getCostTime().hashCode());
        result = prime * result + ((getOperationTime() == null) ? 0 : getOperationTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
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
        sb.append(", username=").append(username);
        sb.append(", module=").append(module);
        sb.append(", operation=").append(operation);
        sb.append(", targetType=").append(targetType);
        sb.append(", targetId=").append(targetId);
        sb.append(", targetName=").append(targetName);
        sb.append(", requestMethod=").append(requestMethod);
        sb.append(", requestUrl=").append(requestUrl);
        sb.append(", requestIp=").append(requestIp);
        sb.append(", requestLocation=").append(requestLocation);
        sb.append(", userAgent=").append(userAgent);
        sb.append(", requestParams=").append(requestParams);
        sb.append(", beforeData=").append(beforeData);
        sb.append(", afterData=").append(afterData);
        sb.append(", status=").append(status);
        sb.append(", errorMessage=").append(errorMessage);
        sb.append(", costTime=").append(costTime);
        sb.append(", operationTime=").append(operationTime);
        sb.append(", createTime=").append(createTime);
        sb.append("]");
        return sb.toString();
    }
}