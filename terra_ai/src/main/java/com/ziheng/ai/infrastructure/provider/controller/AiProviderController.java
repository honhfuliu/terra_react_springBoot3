package com.ziheng.ai.infrastructure.provider.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.ai.infrastructure.provider.domain.dto.AiProviderQuery;
import com.ziheng.ai.infrastructure.provider.domain.dto.AiProviderSaveDTO;
import com.ziheng.ai.infrastructure.provider.domain.vo.AiProviderVO;
import com.ziheng.ai.infrastructure.provider.service.AiProviderService;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysdept.service.SysDeptService;
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

/**
 * AI模型提供商管理
 */
@RestController
@Tag(name = "AI模型提供商管理")
@RequestMapping("/ai/provider")
public class AiProviderController {

    private final AiProviderService aiProviderService;

    public AiProviderController(AiProviderService aiProviderService) {
        this.aiProviderService = aiProviderService;
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询提供商")
    @SaCheckPermission("ai:provider:list")
    public R<PageResult<AiProviderVO>> page(AiProviderQuery query) {
        return R.ok(aiProviderService.page(query));
    }

    /**
     * 新增/修改提供商（id为空时新增，id不为空时修改）
     */
    @PostMapping
    @Operation(summary = "保存提供商（新增或修改）")
    @SaCheckPermission(value = {"ai:provider:add", "ai:provider:edit"}, mode = SaMode.OR)
    @OperateLog(module = OperateModule.AI_PROVIDER,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, service = AiProviderService.class)
    public R<Long> save(@Valid @RequestBody AiProviderSaveDTO dto) {
        return R.ok(aiProviderService.saveProvider(dto));
    }

    /**
     * 删除提供商
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除提供商")
    @SaCheckPermission("ai:provider:delete")
    @OperateLog(module = OperateModule.AI_PROVIDER,idSource = IdSource.PATH,
               type = OperateType.DELETE, service =AiProviderService.class)
    public R<Void> remove(@PathVariable("id") Long id) {
        aiProviderService.delete(id);
        return R.ok();
    }



}
