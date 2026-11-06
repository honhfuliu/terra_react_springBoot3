package com.ziheng.system.sysoperationlog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import com.ziheng.system.sysoperationlog.domain.dto.SysOperationLogQuery;
import com.ziheng.system.sysoperationlog.domain.vo.SysOperationLogVo;
import com.ziheng.system.sysoperationlog.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Tag(name = "操作日志")
@RequestMapping("/system/operationLog")
public class SysOperationLogController {

    private final SysOperationLogService sysOperationLogService;

    public SysOperationLogController(SysOperationLogService sysOperationLogService) {
        this.sysOperationLogService = sysOperationLogService;
    }

    /**
     * 操作日志分页查询
     */
    @SaCheckPermission("system:operationLog:list")
    @GetMapping("/list")
    @Operation(summary = "操作日志查询")
    public R<PageResult<SysOperationLogVo>> list(SysOperationLogQuery query) {
        return R.ok(sysOperationLogService.listOperationLogs(query));
    }

    /**
     * 清空所有操作日志。
     * <p>这里的 &#64;OperateLog 不是随手加的：清空发生在方法内部，
     * 而日志是方法返回后才落库，所以这条"清空"记录会留下来，
     * 将来翻日志还能查到"谁在哪一天清空了日志"。</p>
     *
     * <p>recordBefore / recordAfter 关掉：整表清空没有"某条业务数据"可查。</p>
     */
    @SaCheckPermission("system:operationLog:clear")
    @PostMapping("/clear")
    @Operation(summary = "清空操作日志")
    public R<Integer> clear() {
        return R.ok(sysOperationLogService.clearOperationLogs());
    }

    /**
     * 批量删除操作日志，
     */
    @SaCheckPermission("system:operationLog:delete")
    @PostMapping("/delete")
    @Operation(summary = "批量删除操作日志")
    public R<Integer> delete(@RequestParam("ids") List<Long> ids) {
        return R.ok(sysOperationLogService.deleteOperationLogs(ids));
    }

    /**
     * 导出操作日志（Excel）
     */
    @SaCheckLogin
    @GetMapping("/export")
    @SaCheckPermission("system:operationLog:export")
    @Operation(summary = "导出操作日志")
    public void export(SysOperationLogQuery query, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = "操作日志_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        sysOperationLogService.export(query, response);
    }
}
