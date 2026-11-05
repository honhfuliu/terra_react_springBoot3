package com.ziheng.system.sysmonitor.domain.vo.usermonitor;

import lombok.Data;

/**
 * 在线用户统计信息
 */
@Data

public class OnlineUserStatisticsVO {
    /** 在线用户数量：不同用户去重 */
    private Long onlineUserCount;

    /** 在线会话数量，可作为在线设备数量的近似值 */
    private Long activeDeviceCount;

    /** 即将过期的在线会话数量 */
    private Long expiringDeviceCount;

}
