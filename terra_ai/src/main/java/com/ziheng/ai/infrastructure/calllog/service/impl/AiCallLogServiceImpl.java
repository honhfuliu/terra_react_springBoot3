package com.ziheng.ai.infrastructure.calllog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziheng.ai.common.enums.AiCallStatusEnum;
import com.ziheng.ai.infrastructure.calllog.domain.dto.AiCallLogQuery;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogStatsVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiModelConsumptionVO;
import com.ziheng.ai.infrastructure.calllog.mapper.AiCallLogMapper;
import com.ziheng.ai.infrastructure.calllog.domain.AiCallLog;
import com.ziheng.ai.infrastructure.calllog.service.AiCallLogService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.system.sysuser.domain.SysUser;
import com.ziheng.system.sysuser.service.SysUserService;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Administrator
* @description 针对表【ai_call_log(AI调用日志表)】的数据库操作Service实现
* @createDate 2026-11-12 20:44:39
*/
@Service
public class AiCallLogServiceImpl extends ServiceImpl<AiCallLogMapper, AiCallLog>
    implements AiCallLogService{
    private final Logger log = LoggerFactory.getLogger(AiCallLogServiceImpl.class);

    private final SysUserService sysUserService;

    public AiCallLogServiceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 创建 AI 调用日志
     * @param aiCallLog 调用日志实体
     * @return 调用日志ID
     */
    @Override
    public Long createCallLog(AiCallLog aiCallLog) {
        // 设置调用状态：进行中
        aiCallLog.setStatus(AiCallStatusEnum.RUNNING.getCode());
        // 设置调用开始时间
        aiCallLog.setCreateTime(new Date());
        // 保存日志
        baseMapper.insert(aiCallLog);
        return aiCallLog.getId();
    }

    /**
     * AI 调用成功
     * @param logId 调用日志ID
     * @param messageId 消息ID
     */
    @Override
    @Async("aiLogExecutor")
    public void successCallLog(Long logId, Long messageId, Integer promptTokens, Integer completionTokens, Integer totalTokens){
        updateCallLog(logId, AiCallStatusEnum.SUCCESS.getCode(), messageId, null, promptTokens, completionTokens, totalTokens);
    }

    /**
     * AI 调用失败
     * @param logId 调用日志ID
     * @param errorMessage 错误信息
     */
    @Override
    @Async("aiLogExecutor")
    public void failCallLog(Long logId, String errorMessage) {
        updateCallLog(logId, AiCallStatusEnum.FAILED.getCode(), null, errorMessage, null, null, null);
    }
    /**
     * AI 调用取消
     * @param logId 调用日志ID
     */
    @Override
    @Async("aiLogExecutor")
    public void cancelCallLog(Long logId) {
        updateCallLog(logId, AiCallStatusEnum.CANCELLED.getCode(), null, null, null, null, null);
    }

    @Override
    public AiCallLogStatsVO getGlobalStats() {
        AiCallLogStatsVO stats = baseMapper.selectGlobalStats();
        return stats == null ? new AiCallLogStatsVO() : stats;
    }

    @Override
    public List<AiModelConsumptionVO> getModelConsumption() {
        return baseMapper.selectModelConsumption();
    }

    @Override
    public PageResult<AiCallLogVO> pageLogs(AiCallLogQuery query) {
        Page<AiCallLog> page = Page.of(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiCallLog> wrapper = new LambdaQueryWrapper<AiCallLog>()
                .eq(query.getProviderId() != null, AiCallLog::getProviderId, query.getProviderId())
                .eq(query.getModelId() != null, AiCallLog::getModelId, query.getModelId())
                .eq(StringUtils.hasText(query.getCallType()), AiCallLog::getCallType, query.getCallType())
                .eq(StringUtils.hasText(query.getStatus()), AiCallLog::getStatus, query.getStatus())
                .ge(StringUtils.hasText(query.getBeginTime()), AiCallLog::getCreateTime, query.getBeginTime())
                .le(StringUtils.hasText(query.getEndTime()), AiCallLog::getCreateTime, query.getEndTime())
                .orderByDesc(AiCallLog::getCreateTime)
                .orderByDesc(AiCallLog::getId);

        Page<AiCallLog> result = page(page, wrapper);
        Map<Long, String> nicknameMap = queryNicknames(result.getRecords());
        List<AiCallLogVO> rows = new ArrayList<>(result.getRecords().size());
        for (AiCallLog item : result.getRecords()) {
            AiCallLogVO vo = new AiCallLogVO();
            BeanUtils.copyProperties(item, vo);
            vo.setNickname(nicknameMap.get(item.getUserId()));
            rows.add(vo);
        }
        return PageResult.of(result.getTotal(), rows);
    }

    /**
     * 批量查询调用用户昵称
     */
    private Map<Long, String> queryNicknames(List<AiCallLog> records) {
        List<Long> userIds = new ArrayList<>();
        for (AiCallLog item : records) {
            if (item.getUserId() != null && !userIds.contains(item.getUserId())) {
                userIds.add(item.getUserId());
            }
        }
        Map<Long, String> nicknames = new HashMap<>();
        if (userIds.isEmpty()) {
            return nicknames;
        }
        List<SysUser> users = sysUserService.lambdaQuery()
                .select(SysUser::getUserId, SysUser::getNickname)
                .in(SysUser::getUserId, userIds)
                .list();
        for (SysUser user : users) {
            nicknames.put(user.getUserId(), user.getNickname());
        }
        return nicknames;
    }


    /**
     * 更新 AI 调用日志
     * @param logId 调用日志ID
     * @param status 调用状态
     * @param messageId 消息ID
     * @param errorMessage 错误信息
     */
    private void updateCallLog(Long logId, String status, Long messageId, String errorMessage, Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        log.info("updateCallLog 线程={}, logId={}", Thread.currentThread().getName(), logId);
        try {
            AiCallLog log = baseMapper.selectById(logId);
            if (log == null || !AiCallStatusEnum.RUNNING.getCode().equals(log.getStatus())) {
                return;
            }
            // 计算调用耗时
            long duration = System.currentTimeMillis() - log.getCreateTime().getTime();

            // 更新日志
            AiCallLog updateLog = new AiCallLog();
            updateLog.setStatus(status);
            updateLog.setDuration(duration);
            updateLog.setUpdateTime(new Date());
            updateLog.setMessageId(messageId);
            updateLog.setErrorMessage(errorMessage);
            updateLog.setPromptTokens(promptTokens);
            updateLog.setCompletionTokens(completionTokens);
            updateLog.setTotalTokens(totalTokens);

            // 只允许更新进行中的日志，防止重复结束
            baseMapper.update(updateLog, new LambdaUpdateWrapper<AiCallLog>()
                            .eq(AiCallLog::getId, logId)
                            .eq(AiCallLog::getStatus, AiCallStatusEnum.RUNNING.getCode()));

        } catch (Exception e) {
            log.error("AI调用日志更新失败，logId={}，异常信息：{}", logId, e.getMessage(), e);
        }
    }
}
