package com.ziheng.system.sysloginlog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysloginlog.domain.dto.SysLoginLogQuery;
import com.ziheng.system.sysloginlog.domain.vo.SysLoginLogVo;
import com.ziheng.system.sysloginlog.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@Tag(name = "登录日志")
@RequestMapping("/system/loginLog")
public class SysLoginLogController {

    private final SysLoginLogService sysLoginLogService;

    public SysLoginLogController(SysLoginLogService sysLoginLogService) {
        this.sysLoginLogService = sysLoginLogService;
    }

    /**
     * 登录日志分页查询
     */
    @SaCheckPermission("system:loginLog:list")
    @GetMapping("/list")
    @Operation(summary = "登录日志查询")
    public R<PageResult<SysLoginLogVo>> list(SysLoginLogQuery query) {
        return R.ok(sysLoginLogService.listLoginLogs(query));
    }

    /**
     * 导出登录日志（Excel）
     */
    @SaCheckLogin
    @GetMapping("/export")
    @Operation(summary = "导出登录日志")
    public void export(SysLoginLogQuery query, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = "登录日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        sysLoginLogService.export(query, response);
    }
}
