package com.ziheng.system.sysoperationlog.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperationLogQuery extends PageQuery {

    /**
     * 操作账号（模糊查询）
     */
    private String username;

    /**
     * 业务模块：传枚举名（如 NOTICE）或中文（如 公告管理）都可以
     */
    private String module;

    /**
     * 操作类型：传枚举名（如 DELETE）或中文（如 删除）都可以
     */
    private String operation;

    /**
     * 操作状态：1成功 0失败（与实体一致，用 String 存）
     */
    private String status;

    /**
     * 请求 IP 地址（模糊查询）
     */
    private String requestIp;

    /**
     * 操作时间-开始
     */
    private String startTime;

    /**
     * 操作时间-结束
     */
    private String endTime;
}
