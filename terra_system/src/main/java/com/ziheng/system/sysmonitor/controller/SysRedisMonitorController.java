package com.ziheng.system.sysmonitor.controller;

import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysmonitor.domain.vo.redismonitor.RedisMonitorOverviewVO;
import com.ziheng.system.sysmonitor.service.SysRedisMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/monitor/redis")
@Tag(name = "Redis 监控")
public class SysRedisMonitorController {

    private final SysRedisMonitorService sysRedisMonitorService;

    public SysRedisMonitorController(SysRedisMonitorService sysRedisMonitorService) {
        this.sysRedisMonitorService = sysRedisMonitorService;
    }

    @GetMapping("/overview")
    @Operation(summary = "获取 Redis 监控概览")
    // @SaCheckPermission("system:monitor:redis:list")
    public R<RedisMonitorOverviewVO> getRedisMonitorOverview() {
        return R.ok(sysRedisMonitorService.getRedisMonitorOverview());
    }


}
