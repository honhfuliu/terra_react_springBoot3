package com.ziheng.system.sysoperationlog.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ziheng.system.sysoperationlog.domain.SysOperationLog;
import lombok.Data;

import java.util.Date;

/**
 * 操作日志列表行。
 *
 * <p>字段顺序与列表表头一致：操作账号、业务模块、操作类型、操作对象、
 * 请求信息、状态、耗时、创建时间，外加失败原因（供"操作"列的详情按钮用）。</p>
 */
@Data
public class SysOperationLogVo {

    /**
     * 主键（前端表格 row-key）
     */
    private Long id;

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

    // ==================== 操作对象 ====================

    /**
     * 操作对象类型（NOTICE、USER、ROLE...），第一版暂未写入
     */
    private String targetType;

    /**
     * 操作对象 ID，第一版暂未写入
     */
    private String targetId;

    /**
     * 操作对象名称，第一版暂未写入
     */
    private String targetName;

    // ==================== 请求信息 ====================

    /**
     * 请求方法（GET、POST...）
     */
    private String requestMethod;

    /**
     * 请求 IP
     */
    private String requestIp;

    /**
     * 操作位置（IP 归属地），待接入归属地库后才有值
     */
    private String requestLocation;

    private String requestParams;
    private String requestUrl;

    // ==================== 结果信息 ====================

    /**
     * 操作状态：1成功 0失败
     */
    private String status;

    /**
     * 失败原因
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long costTime;

    /**
     * 创建时间（即操作时间，yyyy-MM-dd HH:mm:ss）
     */
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 操作前数据
     */
    private String beforeData;

    /**
     * 操作后数据
     */
    private String afterData;

    public static SysOperationLogVo from(SysOperationLog log) {
        SysOperationLogVo vo = new SysOperationLogVo();
        vo.setId(log.getId());
        vo.setUsername(log.getUsername());
        vo.setModule(log.getModule());
        vo.setOperation(log.getOperation());
        vo.setTargetType(log.getTargetType());
        vo.setTargetId(log.getTargetId());
        vo.setTargetName(log.getTargetName());
        vo.setRequestMethod(log.getRequestMethod());
        vo.setRequestIp(log.getRequestIp());
        vo.setRequestLocation(log.getRequestLocation());
        vo.setRequestParams(log.getRequestParams());
        vo.setRequestUrl(log.getRequestUrl());
        vo.setStatus(log.getStatus());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setBeforeData(log.getBeforeData());
        vo.setAfterData(log.getAfterData());
        vo.setCostTime(log.getCostTime());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
