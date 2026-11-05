package com.ziheng.system.sysmonitor.service.impl;


import com.ziheng.system.sysmonitor.domain.vo.servermonitor.ApplicationMonitorVO;
import com.ziheng.system.sysmonitor.domain.vo.servermonitor.JvmMonitorVO;
import com.ziheng.system.sysmonitor.domain.vo.servermonitor.ServerMonitorOverviewVO;
import com.ziheng.system.sysmonitor.domain.vo.servermonitor.ServerMonitorVO;
import com.ziheng.system.sysmonitor.service.ServerMonitorService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.lang.management.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务器监控服务实现类
 */
@Service
public class ServerMonitorServiceImpl implements ServerMonitorService {

    private final MeterRegistry meterRegistry; // 作用: 用于注册和管理指标
    private final Environment environment; // 作用: 获取环境信息
    private final HealthEndpoint healthEndpoint; // 作用: 获取健康信息

    public ServerMonitorServiceImpl(MeterRegistry meterRegistry, Environment environment, HealthEndpoint healthEndpoint) {
        this.meterRegistry = meterRegistry;
        this.environment = environment;
        this.healthEndpoint = healthEndpoint;
    }



    /**
     * 获取服务器资源信息
     */
    private ServerMonitorVO getServerResource() {

        ServerMonitorVO vo = new ServerMonitorVO();

        SystemInfo systemInfo = new SystemInfo();

        // 1. CPU
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        vo.setCpuCores(processor.getLogicalProcessorCount());

        // CPU 使用率采样约 1 秒
        double cpuLoad = processor.getSystemCpuLoad(1000L);

        vo.setCpuUsage(cpuLoad >= 0 ? round2(cpuLoad * 100) : 0);

        // 2. 系统物理内存
        GlobalMemory memory = systemInfo.getHardware().getMemory();

        long memoryTotal = memory.getTotal();
        long memoryUsed = memoryTotal - memory.getAvailable();

        vo.setMemoryTotal(toGB(memoryTotal));
        vo.setMemoryUsed(toGB(memoryUsed));
        vo.setMemoryUsage(memoryTotal > 0 ? round2(memoryUsed * 100.0 / memoryTotal) : 0);

        // 3. 磁盘
        // 先获取系统根目录所在的分区
        File[] roots = File.listRoots();

        if (roots != null && roots.length > 0) {
            File root = roots[0];

            long diskTotal = root.getTotalSpace();
            long diskUsed = diskTotal - root.getUsableSpace();


            vo.setDiskTotal(toGB(diskTotal));
            vo.setDiskUsed(toGB(diskUsed));
            vo.setDiskUsage(diskTotal > 0 ? round2(diskUsed * 100.0 / diskTotal) : 0);
        }

        return vo;
    }

    private double toGB(long bytes) {
        return round2(bytes / (1024.0 * 1024 * 1024));
    }



    /**
     * 获取 JVM 监控信息
     */
    private JvmMonitorVO getJvmMonitor() {

        JvmMonitorVO vo = new JvmMonitorVO();

        // 1. JVM 堆内存
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();

        long heapUsed = heap.getUsed();
        long heapMax = heap.getMax();

        vo.setHeapUsed(toMB(heapUsed));
        vo.setHeapMax(toMB(heapMax));

        vo.setHeapUsage(
                heapMax > 0
                        ? round2(heapUsed * 100.0 / heapMax)
                        : 0
        );

        // 2. 非堆内存
        MemoryUsage nonHeap =
                memoryMXBean.getNonHeapMemoryUsage();

        vo.setNonHeapUsed(toMB(nonHeap.getUsed()));

        // 3. 当前线程数
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        vo.setThreadCount(threadMXBean.getThreadCount());

        // 4. GC 总次数
        long gcCount = 0;

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            // -1 表示当前收集器不支持统计
            if (count >= 0) {
                gcCount += count;
            }
        }

        vo.setGcCount(gcCount);

        // 5. JVM 启动以来的运行时长
        long uptime = ManagementFactory
                .getRuntimeMXBean()
                .getUptime();

        vo.setUptime(uptime);

        return vo;
    }

    /**
     * 字节转换为 MB
     */
    private double toMB(long bytes) {
        return round2(bytes / (1024.0 * 1024));
    }

    /**
     * 保留两位小数
     */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * 获取应用服务信息
     */
    private ApplicationMonitorVO getApplicationMonitor() {

        ApplicationMonitorVO vo = new ApplicationMonitorVO();

        // 1. 应用名称
        vo.setApplicationName(environment.getProperty("spring.application.name", "application"));

        // 2. 当前激活环境
        String[] profiles = environment.getActiveProfiles();

        vo.setActiveProfile(profiles.length == 0 ? "default" : String.join(",", profiles));

        // 3. 服务端口
        vo.setPort(environment.getProperty("server.port", Integer.class, 8080));

        // 4. 应用运行时间（毫秒）
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        vo.setUptime(uptime);

        // 5. 应用健康状态
        vo.setStatus(healthEndpoint.health().getStatus().getCode());

        // 6. HTTP 请求统计
        long totalRequests = 0;
        double totalResponseTimeMs = 0;

        for (Timer timer : meterRegistry.find("http.server.requests").timers()) {
            totalRequests += timer.count();
            totalResponseTimeMs += timer.totalTime(TimeUnit.MILLISECONDS);
        }

        vo.setTotalRequests(totalRequests);

        // 平均响应时间 = 累计请求耗时 / 请求次数
        vo.setAverageResponseTime(totalRequests > 0 ? round2(totalResponseTimeMs / totalRequests) : 0);

        return vo;
    }

    /**
     * 获取服务器监控 overview 信息
     * @return 服务器监控 overview 信息
     */
    @Override
    public ServerMonitorOverviewVO getOverview() {
        ServerMonitorOverviewVO vo = new ServerMonitorOverviewVO();
        vo.setServer(getServerResource());
        vo.setJvm(getJvmMonitor());
        vo.setApplication(getApplicationMonitor());
        return vo;
    }
}
