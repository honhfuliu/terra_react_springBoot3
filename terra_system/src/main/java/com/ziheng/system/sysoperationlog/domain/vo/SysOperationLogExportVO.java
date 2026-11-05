package com.ziheng.system.sysoperationlog.domain.vo;

import com.ziheng.common.core.enums.ExecuteStatus;
import com.ziheng.common.util.excel.annotation.ExcelColumn;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import com.ziheng.system.sysoperationlog.domain.SysOperationLog;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class SysOperationLogExportVO {

    @ExcelColumn(value = "日志编号", order = 0, width = 15)
    private Long id;

    @ExcelColumn(value = "操作账号", order = 1, width = 20)
    private String username;

    @ExcelColumn(value = "业务模块", order = 2, width = 20)
    private String module;

    @ExcelColumn(value = "操作类型", order = 3, width = 15)
    private String operation;

    @ExcelColumn(value = "操作对象类型", order = 4, width = 18)
    private String targetType;

    @ExcelColumn(value = "操作对象ID", order = 5, width = 18)
    private String targetId;

    @ExcelColumn(value = "操作对象名称", order = 6, width = 25)
    private String targetName;

    @ExcelColumn(value = "请求方法", order = 7, width = 12)
    private String requestMethod;

    @ExcelColumn(value = "请求URL", order = 8, width = 35)
    private String requestUrl;

    @ExcelColumn(value = "操作IP", order = 9, width = 18)
    private String requestIp;

    @ExcelColumn(value = "操作状态", order = 10, width = 12)
    private String status;

    @ExcelColumn(value = "执行耗时(ms)", order = 11, width = 15)
    private Long costTime;

    @ExcelColumn(
        value = "操作时间",
        order = 12,
        width = 22,
        format = "yyyy-MM-dd HH:mm:ss"
    )
    private Date operationTime;

    /**
     * 批量将操作日志实体转换为导出 VO，状态同步转换为中文展示
     */
    public static List<SysOperationLogExportVO> fromList(List<SysOperationLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysOperationLogExportVO> result = new ArrayList<>(logs.size());
        for (SysOperationLog log : logs) {
            SysOperationLogExportVO vo = new SysOperationLogExportVO();
            vo.setId(log.getId());
            vo.setUsername(log.getUsername());
            vo.setModule(OperateModule.getDescription(log.getModule()));
            vo.setOperation(OperateType.getDescription(log.getOperation()));
            vo.setTargetType(log.getTargetType());
            vo.setTargetId(log.getTargetId());
            vo.setTargetName(log.getTargetName());
            vo.setRequestMethod(log.getRequestMethod());
            vo.setRequestUrl(log.getRequestUrl());
            vo.setRequestIp(log.getRequestIp());
            // 操作状态：0->失败，1->成功
            vo.setStatus(ExecuteStatus.getDescription(log.getStatus()));
            vo.setCostTime(log.getCostTime());
            // 操作时间取 createTime（operationTime 字段当前未被写入，沿用与列表一致的创建时间）
            vo.setOperationTime(log.getCreateTime());
            result.add(vo);
        }
        return result;
    }
}
