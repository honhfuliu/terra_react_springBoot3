package com.ziheng.system.sysloginlog.domain.dto;

import com.ziheng.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysLoginLogQuery extends PageQuery {

    /**
     * 登录账号（模糊查询）
     */
    private String username;

    /**
     * 登录状态：1成功 0失败（CHAR(1)）
     */
    private String status;

    /**
     * 登录时间-开始
     */
    private String startTime;

    /**
     * 登录时间-结束
     */
    private String endTime;
}
