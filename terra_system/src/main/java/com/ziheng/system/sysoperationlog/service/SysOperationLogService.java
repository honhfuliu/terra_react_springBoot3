package com.ziheng.system.sysoperationlog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysoperationlog.domain.SysOperationLog;
import com.ziheng.system.sysoperationlog.domain.dto.SysOperationLogQuery;
import com.ziheng.system.sysoperationlog.domain.vo.SysOperationLogVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sys_operation_log(系统操作审计日志)】的数据库操作Service
* @createDate 2026-09-09 21:40:43
*/
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志
     *
     * @param query 查询条件（操作账号、业务模块、操作类型、操作状态、请求IP、操作时间区间 + 分页参数）
     * @return 分页结果
     */
    PageResult<SysOperationLogVo> listOperationLogs(SysOperationLogQuery query);

    /**
     * 清空所有操作日志
     *
     * @return 被清掉的日志条数
     */
    int clearOperationLogs();

    /**
     * 批量删除操作日志
     *
     * @param ids 日志ID列表
     * @return 实际删除的条数
     */
    int deleteOperationLogs(List<Long> ids);

    /**
     * 导出操作日志（Excel）
     * @param query 查询条件
     * @param response 响应输出流
     */
    void export(SysOperationLogQuery query, HttpServletResponse response);
}
