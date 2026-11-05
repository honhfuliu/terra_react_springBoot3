package com.ziheng.system.sysrole.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysnotice.service.SysNoticeService;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import com.ziheng.system.sysrole.domain.dto.SysRoleAddDTO;
import com.ziheng.system.sysrole.domain.dto.SysRoleQuery;
import com.ziheng.system.sysrole.domain.vo.SysRoleListVO;
import com.ziheng.system.sysrole.domain.vo.SysRoleOptionVO;
import com.ziheng.system.sysrole.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Tag(name = "角色管理")
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 新增或修改角色（dto.roleId 为空则新增，不为空则修改），同时保存角色菜单权限
     */
    @SaCheckPermission(value = {"system:role:add", "system:role:edit"}, mode = SaMode.OR)
    @PostMapping
    @Operation(summary = "新增或修改角色")
    @OperateLog(module = OperateModule.ROLE,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "roleId", service = SysRoleService.class)
    public R<Long> add(@Valid @RequestBody SysRoleAddDTO dto) {
        return R.ok(sysRoleService.addRole(dto));
    }

    /**
     * 分页查询角色列表
     */
    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询角色列表")
    public R<PageResult<SysRoleListVO>> list(SysRoleQuery query) {
        return R.ok(sysRoleService.listRoles(query));
    }

    /**
     * 查询角色选项（用于前端角色选择框，仅返回角色ID与名称）
     */
    @GetMapping("/options")
    @Operation(summary = "查询角色选项")
    public R<List<SysRoleOptionVO>> options() {
        return R.ok(sysRoleService.listRoleOptions());
    }

    /**
     * 查询角色详情（编辑回显）
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "查询角色详情")
    public R<SysRoleAddDTO> info(@PathVariable("roleId") Long roleId) {
        return R.ok(sysRoleService.getRoleDetail(roleId));
    }

    /**
     * 删除角色（支持单条或批量，body 传角色ID数组）
     */
    @SaCheckPermission("system:role:delete")
    @PostMapping("/delete")
    @Operation(summary = "删除角色")
    @OperateLog(module = OperateModule.ROLE,idSource = IdSource.BODY,
            type = OperateType.DELETE, idField = "roleId", service = SysRoleService.class)
    public R<Void> remove(@RequestBody List<Long> roleIds) {
        sysRoleService.deleteRoles(roleIds);
        return R.ok();
    }

    /**
     * 导出角色列表（Excel）
     */
    @SaCheckLogin
    @GetMapping("/export")
    @Operation(summary = "导出角色")
    public void export(SysRoleQuery query, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = "角色列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        sysRoleService.export(query, response);
    }
}
