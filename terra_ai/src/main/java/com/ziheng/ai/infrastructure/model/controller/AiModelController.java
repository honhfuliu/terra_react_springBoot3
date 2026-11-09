package com.ziheng.ai.infrastructure.model.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.ai.infrastructure.model.domain.dto.AiModelQuery;
import com.ziheng.ai.infrastructure.model.domain.dto.AiModelSaveDTO;
import com.ziheng.ai.infrastructure.model.domain.vo.AiModelVO;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai/model")
@Tag(name = "AI模型管理")
public class AiModelController {

    private final AiModelService aiModelService;

    public AiModelController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询模型")
    public R<PageResult<AiModelVO>> page(AiModelQuery query) {
        return R.ok(aiModelService.page(query));
    }

    /**
     * 新增/修改模型（id为空时新增，id不为空时修改）
     */
    @PostMapping
    @Operation(summary = "保存模型（新增或修改）")
    // @SaCheckPermission(value = {"ai:model:add", "ai:model:edit"}, mode = SaMode.OR)
    @OperateLog(module = OperateModule.AI_MODEL, idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, service = AiModelService.class)
    public R<Long> save(@Valid @RequestBody AiModelSaveDTO dto) {
        return R.ok(aiModelService.saveModel(dto));
    }

    /**
     * 删除模型（物理删除）
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除模型")
    @OperateLog(module = OperateModule.AI_MODEL, idSource = IdSource.PATH,
            type = OperateType.DELETE, service = AiModelService.class)
    public R<Void> remove(@PathVariable("id") Long id) {
        aiModelService.delete(id);
        return R.ok();
    }
}
