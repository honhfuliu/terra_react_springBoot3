package com.ziheng.system.sysmonitor.controller;

import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysmonitor.domain.vo.databasemonitor.DatabaseMonitorVO;
import com.ziheng.system.sysmonitor.service.SysDatabaseMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/database/monitor")
@Tag(name = "数据库监控")
public class SysDatabaseMonitorController {
    private final SysDatabaseMonitorService sysDatabaseMonitorService;

    public SysDatabaseMonitorController(SysDatabaseMonitorService sysDatabaseMonitorService) {
        this.sysDatabaseMonitorService = sysDatabaseMonitorService;
    }

    @GetMapping("/overview")
    @Operation(summary = "获取数据库监控概览")
    public R<DatabaseMonitorVO> getDatabaseMonitorOverview() {
        return R.ok(sysDatabaseMonitorService.getDatabaseMonitor());
    }
}
