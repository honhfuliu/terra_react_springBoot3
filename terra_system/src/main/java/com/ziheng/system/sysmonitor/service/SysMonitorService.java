package com.ziheng.system.sysmonitor.service;

import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysmonitor.domain.dto.OnlineQuery;
import com.ziheng.system.sysmonitor.domain.vo.usermonitor.OnlineSessionVO;
import com.ziheng.system.sysmonitor.domain.vo.usermonitor.OnlineUserStatisticsVO;

public interface SysMonitorService {
    /**
     * 获取在线用户统计信息
     */
    OnlineUserStatisticsVO getOnlineUserStatistics();

    PageResult<OnlineSessionVO> getOnlineUserPage(OnlineQuery onlineQuery);

    void forceLogout(Long loginLogId);
}
