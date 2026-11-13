package com.ziheng.ai.infrastructure.calllog.service;

import com.ziheng.ai.infrastructure.calllog.domain.AiCallLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziheng.ai.infrastructure.calllog.domain.dto.AiCallLogQuery;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogStatsVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiModelConsumptionVO;
import com.ziheng.common.core.domain.PageResult;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_call_log(AI调用日志表)】的数据库操作Service
* @createDate 2026-11-12 20:44:39
*/
public interface AiCallLogService extends IService<AiCallLog> {
    /**
     * 创建 AI 调用日志
     */
    Long createCallLog(AiCallLog aiCallLog);

    /**
     * AI 调用成功
     */
    void successCallLog(Long logId, Long messageId, Integer promptTokens, Integer completionTokens, Integer totalTokens);

    /**
     * AI 调用失败
     */
    void failCallLog(Long logId, String errorMessage);

    /**
     * AI 调用取消
     */
    void cancelCallLog(Long logId);

    /**
     * 全局调用统计（总调用次数、Token总消耗、成功/失败次数）
     *
     * @return 统计结果
     */
    AiCallLogStatsVO getGlobalStats();

    /**
     * 模型消耗统计（仅成功调用的有效Token，按总消耗降序）
     *
     * @return 各模型消耗列表
     */
    List<AiModelConsumptionVO> getModelConsumption();

    /**
     * 分页筛选查询调用日志
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<AiCallLogVO> pageLogs(AiCallLogQuery query);

}
