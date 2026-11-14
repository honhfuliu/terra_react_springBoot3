package com.ziheng.system.sysmonitor.service.impl;

import com.alibaba.druid.VERSION;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.JdbcSqlStat;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.ziheng.system.sysmonitor.domain.vo.databasemonitor.*;
import com.ziheng.system.sysmonitor.service.SysDatabaseMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class SysDatabaseMonitorServiceImpl implements SysDatabaseMonitorService {
    private static final Logger log = LoggerFactory.getLogger(SysDatabaseMonitorServiceImpl.class);

    private final DruidDataSource dataSource;

    @Value("${spring.datasource.dynamic.druid.stat.slow-sql-millis}")
    private long slowSqlMillis;


    public SysDatabaseMonitorServiceImpl(DynamicRoutingDataSource dynamicRoutingDataSource) {

        // 获取 Dynamic-Datasource 管理的 master 数据源
        DataSource masterDataSource = dynamicRoutingDataSource.getDataSource("master");

        // 判断是否为 ItemDataSource 包装对象
        if (!(masterDataSource instanceof ItemDataSource itemDataSource)) {
            throw new IllegalStateException("master 数据源不是 ItemDataSource");
        }

        // 获取 ItemDataSource 内部真正的连接池
        DataSource realDataSource = itemDataSource.getDataSource();

        // 判断底层连接池是否为 Druid
        if (!(realDataSource instanceof DruidDataSource druidDataSource)) {
            throw new IllegalStateException("master 底层连接池不是 DruidDataSource，实际类型：" + realDataSource.getClass().getName());
        }

        this.dataSource = druidDataSource;

    }


    /**
     * 获取数据库监控数据
     * @return 数据库监控数据
     */
    @Override
    public DatabaseMonitorVO getDatabaseMonitor() {
        DatabaseMonitorVO vo = new DatabaseMonitorVO();
        vo.setOverview(getDatabaseMonitorOverview());
        vo.setRuntime(getDatabaseRuntime());
        vo.setConnectionPoolHealth(getDatabaseConnectionPoolHealth());
        vo.setDataSourceConfig(getDatabaseDataSourceConfig());
        return vo;
    }

    /**
     * 获取数据库数据源配置
     *
     * @return 数据源配置
     */
    private DatabaseDataSourceConfigVO getDatabaseDataSourceConfig() {
        DatabaseDataSourceConfigVO vo = new DatabaseDataSourceConfigVO();
        // 数据源名称
        vo.setDataSourceName(dataSource.getName());
        // 最大连接数
        vo.setMaxActive(dataSource.getMaxActive());
        // 最小空闲连接数
        vo.setMinIdle(dataSource.getMinIdle());
        // 获取连接的最大等待时间，单位毫秒
        vo.setMaxWaitMs(dataSource.getMaxWait());
        // 慢 SQL 阈值，单位毫秒
        vo.setSlowSqlThresholdMs(slowSqlMillis);
        return vo;
    }

    /**
     * 获取数据库连接池健康状态
     * @return
     */
    private DatabaseConnectionPoolHealthVO getDatabaseConnectionPoolHealth() {
        DatabaseConnectionPoolHealthVO vo = new DatabaseConnectionPoolHealthVO();
        // 获取连接池实时数据
        int activeCount = dataSource.getActiveCount();
        int idleCount = dataSource.getPoolingCount();
        int maxActive = dataSource.getMaxActive();
        int waitingCount = dataSource.getNotEmptyWaitThreadCount();

        // 活跃连接数
        vo.setActiveConnections(activeCount);
        // 空闲连接数
        vo.setIdleConnections(idleCount);
        // 剩余可创建连接容量
        vo.setRemainingCapacity(Math.max(0, maxActive - activeCount - idleCount));
        // 数据源已关闭，直接判定为危险
        if (dataSource.isClosed()) {
            vo.setHealthLevel("危险");
            return vo;
        }

        // 计算连接使用率
        double usageRate = maxActive > 0 ? (double) activeCount / maxActive : 1.0;

        // 健康等级判断
        if (usageRate >= 0.90) {
            vo.setHealthLevel("危险");
        } else if (usageRate >= 0.80 || waitingCount > 0) {
            vo.setHealthLevel("警告");
        } else if (usageRate >= 0.70) {
            vo.setHealthLevel("良好");
        } else {
            vo.setHealthLevel("优秀");
        }
        return vo;
    }

    /**
     * 获取数据库运行时数据
     * @return
     */
    private DatabaseRuntimeVO getDatabaseRuntime() {
        log.info("监控数据源：{}", dataSource);
        log.info("数据源对象标识：{}", System.identityHashCode(dataSource));
        log.info("活跃连接：{}", dataSource.getActiveCount());
        log.info("空闲连接：{}", dataSource.getPoolingCount());
        log.info("最大连接：{}", dataSource.getMaxActive());
        DatabaseRuntimeVO vo = new DatabaseRuntimeVO();
        // 数据源名称
        vo.setDataSourceName(dataSource.getName());
        // 连接池状态
        vo.setPoolStatus(dataSource.isClosed() ? "异常" : "运行中");
        // 活跃连接数
        int activeCount = dataSource.getActiveCount();
        vo.setActiveConnections(activeCount);
        // 空闲连接数
        int idleCount = dataSource.getPoolingCount();
        vo.setIdleConnections(idleCount);
        // 最大连接数
        vo.setMaxConnections(dataSource.getMaxActive());
        // 等待连接的线程数
        vo.setWaitingThreads(dataSource.getNotEmptyWaitThreadCount());
        // SQL 平均响应时间（毫秒）
        // 使用 Druid 累计执行时间 / 累计执行次数计算
        vo.setAverageResponseTimeMs(getAverageSqlResponseTime());

//        // 请求统计暂未接入，先使用初始值
//        vo.setRequestCount(0L);
//        vo.setSuccessRate(BigDecimal.ZERO);
//        vo.setAverageRequestTimeMs(BigDecimal.ZERO);
//
//        // 慢 SQL 统计项数量
//        vo.setSlowQueryCount(getSlowSqlCount());
        return vo;
    }

    /**
     * 获取 Druid 统计周期内的平均 SQL 执行耗时（毫秒）
     */
    private BigDecimal getAverageSqlResponseTime() {
        Map<String, JdbcSqlStat> sqlStatMap = dataSource.getSqlStatMap();

        if (sqlStatMap == null || sqlStatMap.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long totalExecuteCount = 0L;
        long totalExecuteMillis = 0L;

        for (JdbcSqlStat sqlStat : sqlStatMap.values()) {
            totalExecuteCount += sqlStat.getExecuteCount();
            totalExecuteMillis += sqlStat.getExecuteMillisTotal();
        }

        if (totalExecuteCount == 0L) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(totalExecuteMillis)
                .divide(BigDecimal.valueOf(totalExecuteCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 获取数据库监控概览
     */
    private DatabaseMonitorOverviewVO getDatabaseMonitorOverview() {
        DatabaseMonitorOverviewVO vo = new DatabaseMonitorOverviewVO();

        // 实例运行模式
        vo.setInstanceMode("单实例运行稳定");
        // 健康状态：后续可以根据真实连接池指标动态计算
        vo.setHealthStatus("HEALTHY");
        // 连接池组件
        vo.setPoolName("Druid");
        // 连接池版本
        vo.setPoolVersion(VERSION.getVersionNumber());
        // 数据源名称
        vo.setDataSourceName(dataSource.getName());

        // 获取连接池实时数据
        int activeCount = dataSource.getActiveCount();
        int maxActive = dataSource.getMaxActive();
        int poolingCount = dataSource.getPoolingCount();
        int notEmptyWaitCount = dataSource.getNotEmptyWaitThreadCount();

        // 连接池状态
        vo.setStatus("正常");
        // 活跃连接数
        vo.setActiveConnections(activeCount);
        // 最大连接数
        vo.setMaxConnections(maxActive);
        // 等待连接的线程数
        vo.setWaitingThreads(notEmptyWaitCount);
        // 空闲连接数
        vo.setIdleConnections(poolingCount);

        // 连接使用率 = 活跃连接数 / 最大连接数 * 100
        BigDecimal usagePercent = BigDecimal.ZERO;
        if (maxActive > 0) {
            usagePercent = BigDecimal.valueOf(activeCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(maxActive), 2, RoundingMode.HALF_UP);
        }
        vo.setConnectionUsagePercent(usagePercent);

        // 暂时占位，下一步实现慢 SQL 统计
        vo.setSlowQueryCount(getSlowSqlCount());

        return vo;
    }


    /**
     * 获取慢 SQL 统计项数量
     * 统计最大执行耗时达到 1 秒的 SQL 统计项
     */
    private long getSlowSqlCount() {
        Map<String, JdbcSqlStat> sqlStatMap = dataSource.getSqlStatMap();
        if (sqlStatMap == null || sqlStatMap.isEmpty()) {
            return 0L;
        }
        return sqlStatMap.values().stream()
                .filter(sqlStat -> sqlStat.getExecuteMillisMax() >= 1000)
                .count();
    }

}
