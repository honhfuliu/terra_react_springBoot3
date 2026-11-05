package com.ziheng.system.sysmonitor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysmonitor.domain.dto.OnlineQuery;
import com.ziheng.system.sysmonitor.domain.vo.usermonitor.OnlineSessionVO;
import com.ziheng.system.sysmonitor.domain.vo.usermonitor.OnlineUserStatisticsVO;
import com.ziheng.system.sysmonitor.service.SysMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/monitor/online")
@Tag(name = "在线用户管理")
public class SysUserMonitorController {
    private static final Logger log = LoggerFactory.getLogger(SysUserMonitorController.class);
    private final SysMonitorService sysMonitorService;

    public SysUserMonitorController(SysMonitorService sysMonitorService) {
        this.sysMonitorService = sysMonitorService;
    }
    /**
     * 在线用户统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "在线用户统计")
    @SaCheckPermission("system:monitor:online:list")
    public R<OnlineUserStatisticsVO> statistics() {
        return R.ok(sysMonitorService.getOnlineUserStatistics());
    }

    @GetMapping("/page")
    @Operation(summary = "在线用户分页查询")
    @SaCheckPermission("system:monitor:online:list")
    public R<PageResult<OnlineSessionVO>> page(OnlineQuery onlineQuery) {
        return R.ok(sysMonitorService.getOnlineUserPage(onlineQuery));
    }

    /**
     * 强制指定设备下线
     */
    @PostMapping("/forceLogout/{loginLogId}")
    @Operation(summary = "强制指定设备下线")
    @SaCheckPermission("system:monitor:online:forceLogout")
    public R<Void> forceLogout(@PathVariable("loginLogId") Long loginLogId) {
        List<String> tokenList = StpUtil.getTokenValueListByLoginId(1);
        System.out.println(tokenList);
        sysMonitorService.forceLogout(loginLogId);
        return R.ok();
    }

}
