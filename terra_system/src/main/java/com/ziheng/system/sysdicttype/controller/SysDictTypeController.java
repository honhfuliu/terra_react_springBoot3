package com.ziheng.system.sysdicttype.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysdicttype.domain.dto.SysDictTypeAddDTO;
import com.ziheng.system.sysdicttype.domain.vo.SysDictTypeListVO;
import com.ziheng.system.sysdicttype.service.SysDictTypeService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典类型管理
 */
@RestController
@Tag(name = "字典类型管理")
@RequestMapping("/system/dict/type")
public class SysDictTypeController {

    private final SysDictTypeService sysDictTypeService;

    public SysDictTypeController(SysDictTypeService sysDictTypeService) {
        this.sysDictTypeService = sysDictTypeService;
    }

    /**
     * 新增或修改字典类型（dictId 为空表示新增，不为空表示修改）
     */
    @PostMapping
    @Operation(summary = "新增或修改字典类型")
    @SaCheckPermission(value = {"system:dict:type:add", "system:dict:type:edit"}, mode = SaMode.OR)
    @OperateLog(module = OperateModule.DICT,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "dictId", service = SysDictTypeService.class)
    public R<Long> add(@Valid @RequestBody SysDictTypeAddDTO dto) {
        return R.ok(sysDictTypeService.addDictType(dto));
    }

    /**
     * 查询字典类型列表（不分页，可按字典名称模糊搜索）
     */
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    @Operation(summary = "查询字典类型列表")
    public R<List<SysDictTypeListVO>> list(@RequestParam(value = "dictName", required = false)  String dictName) {
        return R.ok(sysDictTypeService.listDictTypes(dictName));
    }

    /**
     * 删除字典类型（该类型下存在字典值时不允许删除）
     */
    @SaCheckPermission("system:dict:type:delete")
    @PostMapping("/delete/{dictId}")
    @Operation(summary = "删除字典类型")
    @OperateLog(module = OperateModule.DICT,idSource = IdSource.PATH, type = OperateType.DELETE, idField = "dictId", service = SysDictTypeService.class)
    public R<Void> remove(@PathVariable("dictId") Long dictId) {
        sysDictTypeService.deleteDictType(dictId);
        return R.ok();
    }
}
