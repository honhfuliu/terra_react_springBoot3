package com.ziheng.system.sysdictdata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysdept.service.SysDeptService;
import com.ziheng.system.sysdictdata.domain.dto.SysDictDataAddDTO;
import com.ziheng.system.sysdictdata.domain.vo.SysDictDataEditVO;
import com.ziheng.system.sysdictdata.domain.vo.SysDictDataListVO;
import com.ziheng.system.sysdictdata.domain.vo.SysDictOptionVO;
import com.ziheng.system.sysdictdata.service.SysDictDataService;
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
 * 字典值管理
 */
@RestController
@Tag(name = "字典值管理")
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    private final SysDictDataService sysDictDataService;

    public SysDictDataController(SysDictDataService sysDictDataService) {
        this.sysDictDataService = sysDictDataService;
    }

    /**
     * 新增或修改字典值（dictCode 为空表示新增，不为空表示修改）
     */
    @PostMapping
    @Operation(summary = "新增或修改字典值")
    @SaCheckPermission(value = {"system:dict:data:add", "system:dict:data:edit"}, mode = SaMode.OR)
    @OperateLog(module = OperateModule.DICT,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "dictCode", service = SysDictDataService.class)
    public R<Long> add(@Valid @RequestBody SysDictDataAddDTO dto) {
        return R.ok(sysDictDataService.addDictData(dto));
    }

    /**
     * 根据字典类型ID查询字典值列表（不分页）
     */
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    @Operation(summary = "查询字典值列表")
    public R<List<SysDictDataListVO>> list(@RequestParam("dictId") Long dictId) {
        return R.ok(sysDictDataService.listDictDatas(dictId));
    }

    /**
     * 根据字典类型编码查询启用的字典值（供前端各个页面下拉框/标签展示使用）
     */
    @GetMapping("/type/{dictType}")
    @Operation(summary = "按字典编码查询字典值")
    public R<List<SysDictOptionVO>> options(@PathVariable("dictType") String dictType) {
        return R.ok(sysDictDataService.listDictOptions(dictType));
    }

    /**
     * 根据字典数据ID查询字典值详情（编辑回显）
     */
    @GetMapping("/{dictCode}")
    @Operation(summary = "查询字典值详情")
    public R<SysDictDataEditVO> edit(@PathVariable("dictCode") Long dictCode) {
        return R.ok(sysDictDataService.getDictDataDetail(dictCode));
    }

    /**
     * 删除字典值（支持单条或批量，物理删除）
     */
    @PostMapping("/delete/{dictCode}")
    @Operation(summary = "删除字典值")
    @SaCheckPermission("system:dict:data:delete")
    @OperateLog(module = OperateModule.DICT,idSource = IdSource.PATH,
            type = OperateType.DELETE, idField = "dictCode", service = SysDictDataService.class)
    public R<Void> remove(@PathVariable("dictCode") Long dictCode) {
        sysDictDataService.deleteDictData(dictCode);
        return R.ok();
    }
}
