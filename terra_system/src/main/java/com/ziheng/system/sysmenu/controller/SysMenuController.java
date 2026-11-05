package com.ziheng.system.sysmenu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysdicttype.service.SysDictTypeService;
import com.ziheng.system.sysmenu.domain.SysMenu;
import com.ziheng.system.sysmenu.domain.dto.SysMenuAddDTO;
import com.ziheng.system.sysmenu.domain.dto.SysMenuQuery;
import com.ziheng.system.sysmenu.domain.dto.SysMenuSortDTO;
import com.ziheng.system.sysmenu.domain.vo.SysMenuListVO;
import com.ziheng.system.sysmenu.domain.vo.SysMenuTreeVO;
import com.ziheng.system.sysmenu.service.SysMenuService;
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

@RestController
@Tag(name = "菜单管理")
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    public SysMenuController(SysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    /**
     * 获取菜单树（用于选择上级菜单）
     * @param needRoot 是否需要构建 root 顶级节点（"主类目"，id=0），默认 true
     */
    // @SaCheckPermission("system:menu:list")
    @GetMapping("/options")
    @Operation(summary = "获取菜单树")
    public R<List<SysMenuTreeVO>> options(
            @RequestParam(required = false, defaultValue = "true", value = "needRoot") Boolean needRoot) {
        return R.ok(sysMenuService.listMenuTree(needRoot));
    }

    /**
     * 查询菜单列表（树结构，用于菜单管理表格展示）
     */
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    @Operation(summary = "查询菜单列表")
    public R<List<SysMenuListVO>> list(SysMenuQuery query) {
        return R.ok(sysMenuService.listMenuList(query));
    }

    /**
     * 新增或修改菜单（dto.menuId 为空则新增，不为空则修改）
     */
    @SaCheckPermission(value = {"system:menu:add", "system:menu:edit"}, mode = SaMode.OR)
    @PostMapping
    @Operation(summary = "新增或修改菜单")
    @OperateLog(module = OperateModule.MENU,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "menuId", service = SysMenuService.class)
    public R<Long> add(@Valid @RequestBody SysMenuAddDTO dto) {
        return R.ok(sysMenuService.addMenu(dto));
    }

    /**
     * 查询菜单详情（编辑回显）
     */
    @GetMapping("/{menuId}")
    @Operation(summary = "查询菜单详情")
    public R<SysMenu> info(@PathVariable("menuId") Long menuId) {
        return R.ok(sysMenuService.getMenuById(menuId));
    }

    /**
     * 删除菜单
     */
    @SaCheckPermission("system:menu:delete")
    @PostMapping("/delete/{menuId}")
    @Operation(summary = "删除菜单")
    @OperateLog(module = OperateModule.MENU,idSource = IdSource.PATH,
            type = OperateType.DELETE, idField = "menuId", service = SysMenuService.class)
    public R<Void> remove(@PathVariable("menuId") Long menuId) {
        sysMenuService.deleteMenu(menuId);
        return R.ok();
    }

    /**
     * 批量保存菜单排序（接收树形结构，保存前端传入的 menuSort 值）
     */
    @SaCheckPermission("system:menu:saveSort")
    @PostMapping("/sort")
    @Operation(summary = "批量保存菜单排序")
    public R<Void> sort(@Valid @RequestBody List<SysMenuSortDTO> sortList) {
        sysMenuService.updateMenuSort(sortList);
        return R.ok();
    }
}
