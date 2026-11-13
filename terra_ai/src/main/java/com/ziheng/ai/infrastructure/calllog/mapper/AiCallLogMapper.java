package com.ziheng.ai.infrastructure.calllog.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziheng.ai.infrastructure.calllog.domain.AiCallLog;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogStatsVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiModelConsumptionVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【ai_call_log(AI调用日志表)】的数据库操作Mapper
* @createDate 2026-11-12 20:44:39
* @Entity generator.domain.AiCallLog
*/
public interface AiCallLogMapper extends BaseMapper<AiCallLog> {

    /**
     * 全局调用统计（总调用次数、Token总消耗、成功/失败次数）
     *
     * @return 统计结果
     */
    AiCallLogStatsVO selectGlobalStats();

    /**
     * 模型消耗统计（仅成功调用的有效Token，按总消耗降序）
     *
     * @return 各模型消耗列表
     */
    List<AiModelConsumptionVO> selectModelConsumption();

}
