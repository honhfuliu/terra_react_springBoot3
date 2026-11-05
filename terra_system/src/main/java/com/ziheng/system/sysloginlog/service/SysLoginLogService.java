package com.ziheng.system.sysloginlog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import com.ziheng.system.sysloginlog.domain.dto.SysLoginLogQuery;
import com.ziheng.system.sysloginlog.domain.vo.SysLoginLogVo;
import jakarta.servlet.http.HttpServletResponse;

/**
* @author Administrator
* @description 针对表【sys_login_log(登录日志表)】的数据库操作Service
* @createDate 2026-09-09 21:33:19
*/
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 分页查询登录日志
     *
     * @param query 查询条件（登录账号、登录状态、登录时间区间 + 分页参数）
     * @return 分页结果
     */
    PageResult<SysLoginLogVo> listLoginLogs(SysLoginLogQuery query);

    /**
     * 查询某用户最近一条成功登录记录
     *
     * @param userId 用户ID
     * @return 最近成功登录日志，无记录时返回 {@code null}
     */
    SysLoginLog getLatestSuccess(Long userId);

    /**
     * 导出登录日志（Excel）
     * @param query 查询条件
     * @param response 响应输出流
     */
    void export(SysLoginLogQuery query, HttpServletResponse response);
}
