package com.ziheng.system.sysdept.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysdept.domain.SysDept;
import com.ziheng.system.sysdept.domain.dto.SysDeptAddDTO;
import com.ziheng.system.sysdept.domain.dto.SysDeptQuery;
import com.ziheng.system.sysdept.domain.dto.SysDeptSortDTO;
import com.ziheng.system.sysdept.domain.vo.SysDeptListVO;
import com.ziheng.system.sysdept.domain.vo.SysDeptTreeVO;
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

import java.util.List;

@RestController
@Tag(name = "部门管理")
@RequestMapping("/system/dept")
public class SysDeptController {
    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    /**
     * 新增或修改部门（dto.deptId 为空则新增，不为空则修改）
     */
    @OperateLog(module = OperateModule.DEPT,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "deptId", service = SysDeptService.class)
    @SaCheckPermission(value = {"system:dept:add", "system:dept:edit"}, mode = SaMode.OR)
    @PostMapping
    @Operation(summary = "新增或修改部门")
    public R<Long> add(@Valid @RequestBody SysDeptAddDTO dto) {
        return R.ok(sysDeptService.addDept(dto));
    }

    /**
     * 查询部门详情（编辑回显）
     */
    // @SaCheckPermission("system:dept:query")
    @GetMapping("/{deptId}")
    @Operation(summary = "查询部门详情")
    public R<SysDept> info(@PathVariable("deptId") Long deptId) {
        return R.ok(sysDeptService.getDeptById(deptId));
    }

    /**
     * 删除部门
     */
    @SaCheckPermission("system:dept:delete")
    @PostMapping("/delete/{deptId}")
    @Operation(summary = "删除部门")
    @OperateLog(module = OperateModule.DEPT,idSource = IdSource.PATH,
            type = OperateType.DELETE, idField = "deptId", service = SysDeptService.class)
    public R<Void> remove(@PathVariable("deptId") Long deptId) {
        sysDeptService.deleteDept(deptId);
        return R.ok();
    }

    /**
     * 获取部门树（用于选择上级部门）
     */
    // @SaCheckPermission("system:dept:list")
    @GetMapping("/options")
    @SaCheckLogin()
    @Operation(summary = "获取部门树")
    public R<List<SysDeptTreeVO>> options() {
        return R.ok(sysDeptService.listDeptTree());
    }

    /**
     * 查询部门列表（树结构，用于部门管理表格展示）
     */
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    @Operation(summary = "查询部门列表")
    public R<List<SysDeptListVO>> list(SysDeptQuery query) {
        return R.ok(sysDeptService.listDeptList(query));
    }

    /**
     * 批量保存部门排序（接收树形结构，保存前端传入的 sortOrder 值）
     */
    @PostMapping("/sort")
    @SaCheckPermission("system:dept:saveSort")
    @Operation(summary = "批量保存部门排序")
    public R<Void> sort(@Valid @RequestBody List<SysDeptSortDTO> sortList) {
        sysDeptService.updateDeptSort(sortList);
        return R.ok();
    }
}
