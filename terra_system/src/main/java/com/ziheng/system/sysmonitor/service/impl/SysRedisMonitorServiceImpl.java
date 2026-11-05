package com.ziheng.system.sysmonitor.service.impl;

import com.ziheng.system.sysmonitor.domain.vo.redismonitor.*;
import com.ziheng.system.sysmonitor.service.SysRedisMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * Redis 监控服务实现类
 */
@Slf4j
@Service
public class SysRedisMonitorServiceImpl implements SysRedisMonitorService {
    private final StringRedisTemplate stringRedisTemplate;


    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${monitor.redis.instance-name:redis-default}")
    private String redisInstanceName;

    public SysRedisMonitorServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取 Redis 监控概览
     *
     * @return
     */
    @Override
    public RedisMonitorOverviewVO getRedisMonitorOverview() {
        RedisMonitorOverviewVO vo = new RedisMonitorOverviewVO();
        vo.setInstance(getRedisInstanceVO());
        vo.setHealth(getRedisHealthVO(vo.getInstance()));
        vo.setStats(getRedisStatsVO());
        vo.setCommandStats(getRedisCommandStatsVO());
        return vo;
    }

    /**
     * 实例信息及资源指标
     *
     * @return
     */
    private RedisInstanceVO getRedisInstanceVO() {
        RedisInstanceVO vo = new RedisInstanceVO();
        // 基础配置
        vo.setInstanceName(redisInstanceName);
        vo.setAddress(redisHost + ":" + redisPort);
        vo.setLastCheckTime(LocalDateTime.now());
        try {
            return stringRedisTemplate.execute((RedisCallback<RedisInstanceVO>) connection -> {
                        // 1. 检查 Redis 连接状态
                        String pong = connection.ping();
                        if (!"PONG".equalsIgnoreCase(pong)) {
                            vo.setStatus("DOWN");
                            return vo;
                        }

                        vo.setStatus("UP");

                        // 2. 获取 Redis 服务端信息
                        Properties serverInfo = connection.serverCommands().info("server");

                        if (serverInfo != null) {
                            vo.setVersion(serverInfo.getProperty("redis_version"));
                            vo.setUptimeSeconds(parseLong(serverInfo, "uptime_in_seconds"));
                        }

                        // 3. 获取 Redis 内存信息
                        Properties memoryInfo = connection.serverCommands().info("memory");

                        if (memoryInfo != null) {
                            Long usedMemory = parseLong(memoryInfo, "used_memory");
                            Long maxMemory = parseLong(memoryInfo, "maxmemory");
                            vo.setUsedMemoryBytes(usedMemory);
                            vo.setMaxMemoryBytes(maxMemory);

                            // maxmemory = 0 表示 Redis 未配置内存上限
                            if (usedMemory != null && maxMemory != null && maxMemory > 0) {
                                BigDecimal usagePercent = BigDecimal.valueOf(usedMemory)
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(BigDecimal.valueOf(maxMemory), 2, RoundingMode.HALF_UP);
                                vo.setMemoryUsagePercent(usagePercent);
                            }
                        }

                        // 4. 获取 Redis 统计信息
                        Properties statsInfo = connection.serverCommands().info("stats");

                        if (statsInfo != null) {
                            Long hits = parseLong(statsInfo, "keyspace_hits");
                            Long misses = parseLong(statsInfo, "keyspace_misses");

                            // 计算缓存命中率
                            if (hits != null && misses != null && hits + misses > 0) {
                                BigDecimal hitRate = BigDecimal.valueOf(hits)
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(BigDecimal.valueOf(hits + misses), 2, RoundingMode.HALF_UP);
                                vo.setHitRate(hitRate);
                            }
                        }
                        // 5. 获取当前连接数
                        Properties clientsInfo = connection.serverCommands().info("clients");
                        if (clientsInfo != null) {
                            vo.setConnectedClients(parseLong(clientsInfo, "connected_clients"));
                        }
                        return vo;
                    }
            );
        } catch (Exception e) {
            // Redis 无法连接或监控命令执行失败
            vo.setStatus("DOWN");
            return vo;
        }
    }


    /**
     * 从 Redis INFO 结果中解析 Long 类型数据
     */
    private Long parseLong(Properties properties, String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取 Redis 健康状态
     *
     * @param instanceVO 实例信息及资源指标
     * @return
     */
    private RedisHealthVO getRedisHealthVO(RedisInstanceVO instanceVO) {
        RedisHealthVO vo = new RedisHealthVO();

        // Redis 无法连接，直接判定为严重异常
        if (!"UP".equalsIgnoreCase(instanceVO.getStatus())) {
            vo.setScore(0);
            vo.setLevel("CRITICAL");
            vo.setMessage("Redis 连接异常");
            vo.setDeductionReasons(List.of("Redis 连接失败，健康评分为 0 分"));
            return vo;
        }

        int score = 0;
        List<String> deductionReasons = new ArrayList<>();
        // 1. Redis 连接状态：30 分
        score += 30;

        // 2. 内存使用率：25 分
        BigDecimal memoryUsage = instanceVO.getMemoryUsagePercent();
        if (memoryUsage != null) {
            if (memoryUsage.compareTo(BigDecimal.valueOf(80)) < 0) {
                score += 25;
            } else if (memoryUsage.compareTo(BigDecimal.valueOf(90)) < 0) {
                score += 15;
                deductionReasons.add("内存使用率为 " + memoryUsage + "% ，超过健康阈值 80%，扣除 10 分");
            } else {
                deductionReasons.add("内存使用率为 " + memoryUsage + "% ，达到或超过 90%，扣除 25 分");
            }
        }

        // 3. 缓存命中率：25 分
        BigDecimal hitRate = instanceVO.getHitRate();
        if (hitRate != null) {
            if (hitRate.compareTo(BigDecimal.valueOf(90)) >= 0) {
                score += 25;
            } else if (hitRate.compareTo(BigDecimal.valueOf(80)) >= 0) {
                score += 15;
                deductionReasons.add("缓存命中率为 " + hitRate + "%，低于健康阈值 90%，扣除 10 分");
            } else if (hitRate.compareTo(BigDecimal.valueOf(70)) >= 0) {
                score += 5;
                deductionReasons.add("缓存命中率为 " + hitRate + "%，低于 80%，扣除 20 分");
            } else {
                deductionReasons.add("缓存命中率为 " + hitRate + "%，低于 70%，扣除 25 分");
            }
        }

        // 4. 阻塞客户端：20 分
        Long blockedClients = getRedisBlockedClients();

        if (blockedClients != null) {
            if (blockedClients == 0) {
                score += 20;
            } else if (blockedClients <= 2) {
                score += 10;
                deductionReasons.add("存在 " + blockedClients + " 个阻塞客户端，扣除 10 分");
            } else {
                deductionReasons.add("存在 " + blockedClients + " 个阻塞客户端，扣除 20 分");
            }
        }

        // 设置健康评分
        vo.setScore(score);
        vo.setDeductionReasons(deductionReasons);
        // 设置健康等级及描述
        if (score >= 90) {
            vo.setLevel("GOOD");
            vo.setMessage("Redis 运行良好");
        } else if (score >= 70) {
            vo.setLevel("WARNING");
            vo.setMessage("Redis 存在部分指标异常，请关注");
        } else {
            vo.setLevel("CRITICAL");
            vo.setMessage("Redis 健康状况异常，请及时检查");
        }

        return vo;
    }

    /**
     * 获取 Redis 阻塞客户端数量
     *
     * @return 阻塞客户端数量
     */
    private Long getRedisBlockedClients() {
        return stringRedisTemplate.execute((RedisCallback<Long>) connection -> {
                    Properties clientsInfo = connection.serverCommands().info("clients");
                    if (clientsInfo == null) {
                        return null;
                    }
                    return parseLong(clientsInfo, "blocked_clients");
                }
        );
    }

    /**
     * 当前实例资源概览
     *
     * @return
     */
    private RedisStatsVO getRedisStatsVO() {
        RedisStatsVO vo = new RedisStatsVO();
        return stringRedisTemplate.execute((RedisCallback<RedisStatsVO>) connection -> {
            // 1. 获取当前键数量
            Properties keyspaceInfo = connection.serverCommands().info("keyspace");
            if (keyspaceInfo != null) {
                long keyCount = 0L;
                for (String key : keyspaceInfo.stringPropertyNames()) {
                    // 只处理 db0、db1 等数据库信息
                    if (key.matches("db\\d+")) {
                        String value = keyspaceInfo.getProperty(key);
                        if (value != null) {
                            for (String item : value.split(",")) {
                                String[] pair = item.split("=", 2);

                                if (pair.length == 2 && "keys".equals(pair[0])) {
                                    keyCount += parseLongValue(pair[1]);
                                    break;
                                }
                            }
                        }
                    }
                }

                vo.setKeyCount(keyCount);
            }

            // 2. 获取累计过期键数量
            Properties statsInfo = connection.serverCommands().info("stats");
            if (statsInfo != null) {
                vo.setExpiredKeys(parseLong(statsInfo, "expired_keys"));
            }

            // 3. 获取当前阻塞客户端数量
            Properties clientsInfo = connection.serverCommands().info("clients");
            if (clientsInfo != null) {
                vo.setBlockedClients(parseLong(clientsInfo, "blocked_clients"));
            }

            // 4. 获取 Redis 内存淘汰策略
            try {
                Object result = connection.execute("CONFIG",
                        "GET".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        "maxmemory-policy".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                if(result instanceof byte[] bytes){
                    vo.setEvictionPolicy(new String(bytes, StandardCharsets.UTF_8));
                    log.info("vo.setEvictionPolicy:{}", vo.getEvictionPolicy());
                }
            } catch (Exception e) {
                // Redis ACL 或托管服务可能禁止 CONFIG GET
                vo.setEvictionPolicy(null);
            }

            return vo;
        });

    }

    /**
     * 将字符串解析为 long
     */
    private long parseLongValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 命令统计指标
     *
     * @return
     */
    private List<RedisCommandStatsVO> getRedisCommandStatsVO() {
        List<RedisCommandStatsVO> result  = new ArrayList<>();
        return stringRedisTemplate.execute((RedisCallback<List<RedisCommandStatsVO>>) connection -> {
            Properties commandInfo = connection.serverCommands().info("commandstats");
            if (commandInfo == null || commandInfo.isEmpty()) {
                return result;
            }
            // 需要单独展示的命令
            List<String> targetCommands = List.of("get", "set", "hget", "expire");

            // 命令调用次数、总耗时（微秒）
            Map<String, Long> callsMap = new HashMap<>();
            Map<String, Long> timeMap = new HashMap<>();

            long totalCalls = 0L;
            long totalTimeUs = 0L;

            // 1. 解析所有命令统计数据
            for (String key : commandInfo.stringPropertyNames()) {
                if (!key.startsWith("cmdstat_")) {
                    continue;
                }

                String commandName = key.substring("cmdstat_".length()).toLowerCase();

                String value = commandInfo.getProperty(key);
                if (value == null || value.isBlank()) {
                    continue;
                }

                Map<String, Long> fields = new HashMap<>();

                for (String item : value.split(",")) {
                    String[] pair = item.split("=", 2);

                    if (pair.length == 2) {
                        try {
                            fields.put(pair[0], Long.parseLong(pair[1]));
                        } catch (NumberFormatException ignored) {
                            // 忽略无法解析的字段
                        }
                    }
                }

                long calls = fields.getOrDefault("calls", 0L);
                long timeUs = fields.getOrDefault("usec", 0L);

                callsMap.put(commandName, calls);
                timeMap.put(commandName, timeUs);

                totalCalls += calls;
                totalTimeUs += timeUs;
            }

            if (totalCalls == 0L) {
                return result;
            }

            // 2. 生成 GET、SET、HGET、EXPIRE 四类统计
            long displayedCalls = 0L;
            long displayedTimeUs = 0L;

            for (String command : targetCommands) {
                long calls = callsMap.getOrDefault(command, 0L);
                long timeUs = timeMap.getOrDefault(command, 0L);
                displayedCalls += calls;
                displayedTimeUs += timeUs;
                result.add(buildCommandStats(command.toUpperCase(), calls, timeUs, totalCalls));
            }

            // 3. 汇总其他命令
            long otherCalls = Math.max(0L, totalCalls - displayedCalls);
            long otherTimeUs = Math.max(0L, totalTimeUs - displayedTimeUs);

            result.add(buildCommandStats("其他命令", otherCalls, otherTimeUs , totalCalls));
            return result;
        });
    }

    /**
     * 构建单条命令统计数据
     *
     * @param commandName 命令名称
     * @param calls       调用次数
     * @param timeUs      总耗时（微秒）
     * @param totalCalls  所有命令的总调用次数
     * @return 命令统计 VO
     */
    private RedisCommandStatsVO buildCommandStats(String commandName, long calls, long timeUs, long totalCalls) {

        RedisCommandStatsVO vo = new RedisCommandStatsVO();
        vo.setCommandName(commandName);
        vo.setCalls(calls);
        // 1. 计算命令调用占比（%）
        BigDecimal percentage = BigDecimal.ZERO;

        if (totalCalls > 0) {
            percentage = BigDecimal.valueOf(calls).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCalls), 2, RoundingMode.HALF_UP);
        }

        vo.setPercentage(percentage);

        // 2. 计算平均耗时（微秒转换为毫秒）
        BigDecimal avgTimeMs = BigDecimal.ZERO;

        if (calls > 0) {
            avgTimeMs = BigDecimal.valueOf(timeUs).divide(BigDecimal.valueOf(calls)
                                    .multiply(BigDecimal.valueOf(1000)), 3, RoundingMode.HALF_UP);
        }

        vo.setAvgTimeMs(avgTimeMs);

        return vo;
    }


}
