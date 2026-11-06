package com.ziheng.system.sysmonitor.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.enums.ExecuteStatus;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import com.ziheng.system.sysloginlog.service.SysLoginLogService;
import com.ziheng.system.sysmonitor.domain.dto.OnlineQuery;
import com.ziheng.system.sysmonitor.domain.vo.usermonitor.OnlineSessionVO;
import com.ziheng.system.sysmonitor.domain.vo.usermonitor.OnlineUserStatisticsVO;
import com.ziheng.system.sysmonitor.service.SysMonitorService;
import com.ziheng.system.sysuser.domain.SysUser;
import com.ziheng.system.sysuser.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysMonitorServiceImpl implements SysMonitorService {

    private final SysLoginLogService sysLoginLogService;

    private final SysUserService sysUserService;

    public SysMonitorServiceImpl(SysLoginLogService sysLoginLogService, SysUserService sysUserService) {
        this.sysLoginLogService = sysLoginLogService;
        this.sysUserService = sysUserService;

    }
    /**
     * 获取在线用户统计信息
     * @return
     */
    @Override
    public OnlineUserStatisticsVO getOnlineUserStatistics() {
        List<String> tokenList = StpUtil.searchTokenValue("", 0, -1, false);
        String prefix = "Authorization:login:token:";
        // 用于统计去重后的在线人数
        Set<String> onlineUserIds = new HashSet<>();
        // 即将过期的会话数
        long expiringSessionCount = 0;
        // 统计在线会话数（活跃设备数）
        long onlineSessionCount = 0;
        // 30 分钟，单位：秒
        long expiringThreshold = 30 * 60L;
        for (String token : tokenList) {
            // 兼容搜索结果包含 Redis Key 前缀的情况
            if (token.startsWith(prefix)) {
                token = token.substring(prefix.length());
            }
            // 查询登录 ID，无效登录身份直接跳过
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                continue;
            }
            // 在线人数去重
            onlineUserIds.add(loginId.toString());
            // 有效会话计数，每个有效 Token 计为一个会话
            onlineSessionCount++;
            // 获取剩余有效期
            long timeout = StpUtil.getTokenTimeout(token);
            // timeout > 0 表示有剩余有效期
            if (timeout > 0 && timeout <= expiringThreshold) {
                expiringSessionCount++;
            }
        }

        OnlineUserStatisticsVO vo = new OnlineUserStatisticsVO();
        vo.setOnlineUserCount((long) onlineUserIds.size());
        vo.setExpiringDeviceCount(expiringSessionCount);
        vo.setActiveDeviceCount(onlineSessionCount);
        return vo;
    }

    /**
     * 获取在线用户分页列表
     * @param onlineQuery 在线用户查询条件
     * @return 在线用户分页列表
     */
    @Override
    public PageResult<OnlineSessionVO> getOnlineUserPage(OnlineQuery onlineQuery) {
        List<String> tokenList = StpUtil.searchTokenValue("", 0, -1, false);
        String prefix = "Authorization:login:token:";

        // tokenHash -> 在线会话信息
        Map<String, OnlineSessionVO> sessionMap = new LinkedHashMap<>();
        // tokenHash -> 用户ID（用于后续批量查询昵称/头像，VO 本身不再冗余存储 userId）
        Map<String, Long> userIdMap = new LinkedHashMap<>();
        // 遍历 Token，只保留当前有效会话
        for (String value : tokenList) {
            if (StrUtil.isBlank(value)) {
                continue;
            }
            // 兼容搜索结果为 Redis Key 的情况
            String token = value.startsWith(prefix) ? value.substring(prefix.length()) : value;

            // 判断登录身份是否仍然有效
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                continue;
            }

            // 对真实 Token 计算 SHA-256
            String tokenHash = SecureUtil.sha256(token);

            // 防止重复 Token 被重复统计
            if (sessionMap.containsKey(tokenHash)) {
                continue;
            }

            long timeout = StpUtil.getTokenTimeout(token);

            OnlineSessionVO vo = new OnlineSessionVO();
            vo.setTokenTimeout(timeout);
            sessionMap.put(tokenHash, vo);
            userIdMap.put(tokenHash, Long.valueOf(loginId.toString()));
        }

        // 没有有效会话，直接返回空分页
        if (sessionMap.isEmpty()) {
            return new PageResult<>();
        }

        // 批量查询这些会话对应的成功登录日志
        List<String> tokenHashes = new ArrayList<>(sessionMap.keySet());

        List<SysLoginLog> loginLogs = sysLoginLogService.list(
                Wrappers.<SysLoginLog>lambdaQuery()
                        .in(SysLoginLog::getTokenHash, tokenHashes)
                        .eq(SysLoginLog::getStatus, ExecuteStatus.SUCCESS.getCode())
                        .orderByDesc(SysLoginLog::getLoginTime)
        );

        // 同一个 tokenHash 如果存在多条成功日志，保留最新一条
        Map<String, SysLoginLog> logMap = loginLogs.stream()
                .filter(log -> StrUtil.isNotBlank(log.getTokenHash()))
                .collect(Collectors.toMap(
                        SysLoginLog::getTokenHash,
                        Function.identity(),
                        (oldLog, newLog) -> oldLog,
                        LinkedHashMap::new
                ));

        // 批量查询这些会话对应用户的昵称与头像
        List<Long> userIds = userIdMap.values().stream()
                .distinct()
                .toList();
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(
                        SysUser::getUserId,
                        Function.identity(),
                        (a, b) -> a
                ));

        // 将登录日志信息合并到在线会话
        List<OnlineSessionVO> allSessions = new ArrayList<>();

        for (Map.Entry<String, OnlineSessionVO> entry : sessionMap.entrySet()) {
            String tokenHash = entry.getKey();
            OnlineSessionVO vo = entry.getValue();
            SysLoginLog log = logMap.get(tokenHash);
            // 没有匹配的登录日志，暂时无法可靠地按 IP 筛选
            if (log == null) {
                continue;
            }
            vo.setUsername(log.getUsername());
            vo.setIpAddress(log.getIpAddress());
            vo.setLoginTime(log.getLoginTime());
            vo.setBrowser(log.getBrowser());
            vo.setOs(log.getOs());
            vo.setLoginLocation(log.getLoginLocation());
            vo.setLoginLogId(log.getId());

            // 补充用户昵称与头像
            SysUser user = userMap.get(userIdMap.get(tokenHash));
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            // 按账号和 IP 过滤，两个条件同时传入时使用 AND
            if (StrUtil.isNotBlank(onlineQuery.getUsername())
                    && (StrUtil.isBlank(vo.getUsername())
                    || !vo.getUsername().contains(onlineQuery.getUsername()))) {
                continue;
            }

            if (StrUtil.isNotBlank(onlineQuery.getIp())
                    && (StrUtil.isBlank(vo.getIpAddress())
                    || !vo.getIpAddress().contains(onlineQuery.getIp()))) {
                continue;
            }

            allSessions.add(vo);
        }

        // 按登录时间倒序，最新会话排在前面
        allSessions.sort(
                Comparator.comparing(
                        OnlineSessionVO::getLoginTime,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        // 内存分页
        long total = allSessions.size();
        long startLong = (long) (onlineQuery.getPageNum() - 1) * onlineQuery.getPageSize();

        List<OnlineSessionVO> records;

        if (startLong >= total) {
            records = Collections.emptyList();
        } else {
            int start = (int) startLong;
            int end = (int) Math.min(startLong + onlineQuery.getPageSize(), total);
            records = allSessions.subList(start, end);
        }

        PageResult<OnlineSessionVO> result = new PageResult<>();
        result.setTotal(total);
        result.setRows(records);

        return result;
    }

    @Override
    public void forceLogout(Long loginLogId) {
        LambdaQueryWrapper<SysLoginLog> eq = new LambdaQueryWrapper<SysLoginLog>()
                .select(SysLoginLog::getTokenHash, SysLoginLog::getId, SysLoginLog::getUserId)
                .eq(SysLoginLog::getStatus, ExecuteStatus.SUCCESS.getCode())
                .eq(SysLoginLog::getId, loginLogId);
        SysLoginLog loginLog = sysLoginLogService.getOne(eq);
        if (loginLog == null){
            throw new BusinessException("会话不存在");
        }
        List<String> tokenList = StpUtil.getTokenValueListByLoginId(loginLog.getUserId());
        if (tokenList == null || tokenList.isEmpty()) {
            throw new BusinessException("会话不存在");
        }
        for (String token : tokenList) {
            if (!loginLog.getTokenHash().equals(SecureUtil.sha256(token))) {
                continue;
            }
            // 判断 Token 是否仍然有效
            Object loginId = StpUtil.getLoginIdByToken(token);
            // 确认 Token 属于目标用户
            if (!loginLog.getUserId().toString().equals(loginId.toString())) {
                throw new BusinessException("目标会话与用户不匹配");
            }
            // 注销指定设备
            StpUtil.logoutByTokenValue(token);
            return;
        }
        throw new BusinessException("该设备已下线或会话已失效");
    }


}
