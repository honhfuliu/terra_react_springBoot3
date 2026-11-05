package com.ziheng.system.sysmonitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysmonitor.domain.vo.servermonitor.ServerMonitorOverviewVO;
import com.ziheng.system.sysmonitor.service.ServerMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("system/monitor/server")
@Tag(name = "服务器监控")
public class SysServerMonitorController {

    private final ServerMonitorService serverMonitorService;

    public SysServerMonitorController(ServerMonitorService serverMonitorService) {
        this.serverMonitorService = serverMonitorService;
    }

    /**
     * 获取服务器监控信息
     */
    @GetMapping("/overview")
    @Operation(summary = "获取服务器监控概览信息")
    @SaCheckPermission("system:monitor:server:list")
    public R<ServerMonitorOverviewVO> getServerMonitor() {
        return R.ok(serverMonitorService.getOverview());
    }
}
