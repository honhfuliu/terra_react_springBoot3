package com.ziheng.ai.infrastructure.knowledge.controller;

import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeQuery;
import com.ziheng.ai.infrastructure.knowledge.domain.dto.AiKnowledgeSaveDTO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeDetailVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeListVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeOptionVO;
import com.ziheng.ai.infrastructure.knowledge.domain.vo.AiKnowledgeStatsVO;
import com.ziheng.ai.infrastructure.knowledge.service.AiKnowledgeService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/knowledge")
@Tag(name = "AI知识库管理")
public class AiKnowledgeController {
    private final AiKnowledgeService aiKnowledgeService;

    public AiKnowledgeController(AiKnowledgeService aiKnowledgeService) {
        this.aiKnowledgeService = aiKnowledgeService;
    }

    /**
     * 统计知识库、文档、Chunk总数
     */
    @GetMapping("/stats")
    @Operation(summary = "统计知识库/文档/Chunk总数")
    public R<AiKnowledgeStatsVO> stats() {
        return R.ok(aiKnowledgeService.stats());
    }

    /**
     * 查询所有启用知识库的选项（供前端选择框使用）
     */
    @GetMapping("/options")
    @Operation(summary = "查询启用知识库选项")
    public R<List<AiKnowledgeOptionVO>> options() {
        return R.ok(aiKnowledgeService.listKnowledgeOptions());
    }

    /**
     * 根据ID查询知识库详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询知识库详情")
    public R<AiKnowledgeDetailVO> detail(@PathVariable("id") Long id) {
        return R.ok(aiKnowledgeService.detail(id));
    }

    /**
     * 分页查询知识库列表（keyword 模糊匹配名称或描述）
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询知识库")
    public R<PageResult<AiKnowledgeListVO>> page(AiKnowledgeQuery query) {
        return R.ok(aiKnowledgeService.page(query));
    }

    /**
     * 新增或修改知识库（id为空表示新增，不为空表示修改）
     */
    @PostMapping
    @Operation(summary = "保存知识库（新增或修改）")
    @OperateLog(module = OperateModule.AI_KNOWLEDGE, idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, service = AiKnowledgeService.class)
    public R<Long> save(@Valid @RequestBody AiKnowledgeSaveDTO dto) {
        return R.ok(aiKnowledgeService.saveKnowledge(dto));
    }

    /**
     * 删除知识库
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除知识库")
    @OperateLog(module = OperateModule.AI_KNOWLEDGE, idSource = IdSource.PATH,
            type = OperateType.DELETE, service = AiKnowledgeService.class)
    public R<Void> remove(@PathVariable("id") Long id) {
        aiKnowledgeService.delete(id);
        return R.ok();
    }
}
