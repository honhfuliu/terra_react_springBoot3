package com.ziheng.system.sysnotice.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysdicttype.service.SysDictTypeService;
import com.ziheng.system.sysnotice.domain.dto.SysNoticeAddDTO;
import com.ziheng.system.sysnotice.domain.dto.SysNoticeQuery;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeDetailVo;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeLatestVO;
import com.ziheng.system.sysnotice.domain.vo.SysNoticeVo;
import com.ziheng.system.sysnotice.service.SysNoticeService;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统公告管理
 */
@RestController
@Tag(name = "系统公告")
@RequestMapping("/system/notice")
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;

    public SysNoticeController(SysNoticeService sysNoticeService) {
        this.sysNoticeService = sysNoticeService;
    }

    /**
     * 新增或修改公告（id 为空表示新增，不为空表示修改）
     */
    @PostMapping
    @Operation(summary = "新增或修改公告")
    @SaCheckPermission(value = {"system:notice:add", "system:notice:edit"}, mode = SaMode.OR)
    @OperateLog(module = OperateModule.ANNOUNCEMENT,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "id", service = SysNoticeService.class)
    public R<Long> add(@Valid @RequestBody SysNoticeAddDTO dto) {
        return R.ok(sysNoticeService.addNotice(dto));
    }

    /**
     * 公告分页查询
     */
    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    @Operation(summary = "公告查询")
    public R<PageResult<SysNoticeVo>> list(SysNoticeQuery query) {
        return R.ok(sysNoticeService.listNotices(query));
    }

    /**
     * 查询最新 5 条公告（Header 展示，置顶优先）
     */
    @SaCheckLogin
    @GetMapping("/latest")
    @Operation(summary = "最新公告(置顶优先,取5条)")
    public R<List<SysNoticeLatestVO>> latest() {
        return R.ok(sysNoticeService.latestNotices());
    }

    /**
     * 查询公告详情（编辑回显）
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询公告详情")
    @SaCheckPermission("system:notice:edit")
    public R<SysNoticeDetailVo> info(@PathVariable("id") Long id) {
        return R.ok(sysNoticeService.getNoticeDetail(id));
    }

    /**
     * 删除公告
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除公告")
    @SaCheckPermission("system:notice:delete")
    @OperateLog(module = OperateModule.ANNOUNCEMENT,idSource = IdSource.PATH,
            type = OperateType.DELETE, idField = "id", service = SysNoticeService.class)
    public R<Void> remove(@PathVariable("id") Long id) {
        sysNoticeService.deleteNotice(id);
        return R.ok();
    }

    /**
     * 置顶公告（原置顶自动取消）
     */
    @PostMapping("/top/{id}")
    @Operation(summary = "置顶公告")
    @SaCheckPermission("system:notice:edit")
    @OperateLog(module = OperateModule.ANNOUNCEMENT,idSource = IdSource.PATH,
            type = OperateType.UPDATE, idField = "id", service = SysNoticeService.class)
    public R<Void> top(@PathVariable("id") Long id) {
        sysNoticeService.topNotice(id);
        return R.ok();
    }

    /**
     * 批量发布公告
     */
    @PostMapping("/publish")
    @Operation(summary = "批量发布公告")
    @SaCheckPermission("system:notice:edit")
    @OperateLog(module = OperateModule.ANNOUNCEMENT,idSource = IdSource.BODY,
            type = OperateType.UPDATE, idField = "ids", service = SysNoticeService.class)
    public R<Void> publish(@RequestBody List<Long> ids) {
        sysNoticeService.publishNotices(ids);
        return R.ok();
    }

    /**
     * 下架公告
     */
    @PostMapping("/offline/{id}")
    @Operation(summary = "下架公告")
    @SaCheckPermission("system:notice:edit")
    @OperateLog(module = OperateModule.ANNOUNCEMENT,idSource = IdSource.PATH,
            type = OperateType.UPDATE, idField = "id", service = SysNoticeService.class)
    public R<Void> offline(@PathVariable("id") Long id) {
        sysNoticeService.offlineNotice(id);
        return R.ok();
    }
}
