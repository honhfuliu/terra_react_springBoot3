package com.ziheng.ai.infrastructure.calllog.controller;

import com.ziheng.ai.infrastructure.calllog.domain.dto.AiCallLogQuery;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogStatsVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiCallLogVO;
import com.ziheng.ai.infrastructure.calllog.domain.vo.AiModelConsumptionVO;
import com.ziheng.ai.infrastructure.calllog.service.AiCallLogService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ai/call/log")
@Tag(name = "AI调用日志接口")
public class CallLogController {
    private final AiCallLogService aiCallLogService;

    public CallLogController(AiCallLogService aiCallLogService) {
        this.aiCallLogService = aiCallLogService;
    }

    /**
     * 全局调用统计
     */
    @GetMapping("/stats")
    @Operation(summary = "全局调用统计")
    public R<AiCallLogStatsVO> globalStats() {
        return R.ok(aiCallLogService.getGlobalStats());
    }

    /**
     * 模型消耗统计（按总Token降序）
     */
    @GetMapping("/stats/model")
    @Operation(summary = "模型消耗统计")
    public R<List<AiModelConsumptionVO>> modelConsumption() {
        return R.ok(aiCallLogService.getModelConsumption());
    }

    /**
     * 调用日志筛选分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询调用日志")
    public R<PageResult<AiCallLogVO>> page(AiCallLogQuery query) {
        return R.ok(aiCallLogService.pageLogs(query));
    }

}
