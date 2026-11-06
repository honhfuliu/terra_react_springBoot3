package com.ziheng.system.sysloginlog.domain.vo;

import com.ziheng.common.core.enums.ExecuteStatus;
import com.ziheng.common.util.excel.annotation.ExcelColumn;
import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 登录日志导出 VO
 */
@Data
public class SysLoginLogExportVO {

    @ExcelColumn(value = "日志编号", order = 0, width = 15)
    private Long id;

    @ExcelColumn(value = "登录账号", order = 1, width = 20)
    private String username;

    @ExcelColumn(value = "登录IP", order = 2, width = 20)
    private String ipAddress;

    @ExcelColumn(value = "登录地点", order = 3, width = 20)
    private String loginLocation;

    @ExcelColumn(value = "浏览器", order = 4, width = 15)
    private String browser;

    @ExcelColumn(value = "操作系统", order = 5, width = 20)
    private String os;

    @ExcelColumn(value = "登录方式", order = 6, width = 20)
    private String loginType;

    @ExcelColumn(value = "登录状态", order = 7, width = 12)
    private String status;

    @ExcelColumn(value = "操作信息", order = 8, width = 30)
    private String message;

    @ExcelColumn(value = "登录时间", order = 9, width = 22, format = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;

    public static List<SysLoginLogExportVO> fromList(List<SysLoginLog> logs) {
        if (logs == null || logs.isEmpty()) return Collections.emptyList();
        List<SysLoginLogExportVO> result = new ArrayList<>(logs.size());
        for (SysLoginLog log : logs) {
            SysLoginLogExportVO vo = new SysLoginLogExportVO();
            vo.setId(log.getId());
            vo.setUsername(log.getUsername());
            vo.setIpAddress(log.getIpAddress());
            vo.setLoginLocation(log.getLoginLocation());
            vo.setBrowser(log.getBrowser());
            vo.setOs(log.getOs());
            vo.setLoginType(log.getLoginType());
            vo.setStatus(ExecuteStatus.getDescription(log.getStatus()));
            vo.setMessage(log.getMessage());
            vo.setLoginTime(log.getLoginTime());
            result.add(vo);
        }
        return result;
    }
}
