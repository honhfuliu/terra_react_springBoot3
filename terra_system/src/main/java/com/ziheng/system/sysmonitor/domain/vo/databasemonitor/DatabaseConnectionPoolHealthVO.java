package com.ziheng.system.sysmonitor.domain.vo.databasemonitor;

import lombok.Data;

/**
 * 数据库连接池健康信息VO
 */
@Data
public class DatabaseConnectionPoolHealthVO {

    /** 活跃连接数 */
    private Integer activeConnections;

    /** 空闲连接数 */
    private Integer idleConnections;

    /** 剩余可创建连接数 */
    private Integer remainingCapacity;

    /** 连接池健康度：优秀、良好、警告、危险 */
    private String healthLevel;
}
