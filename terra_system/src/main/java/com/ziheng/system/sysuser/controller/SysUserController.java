package com.ziheng.system.sysuser.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.ziheng.system.security.util.LoginUserUtils;
import com.ziheng.system.sysuser.domain.vo.SysUserProfileVO;
import com.ziheng.common.core.domain.PageResult;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.sysnotice.service.SysNoticeService;
import com.ziheng.system.sysoperationlog.annotation.OperateLog;
import com.ziheng.system.sysoperationlog.constant.IdSource;
import com.ziheng.system.sysoperationlog.constant.OperateModule;
import com.ziheng.system.sysoperationlog.constant.OperateType;
import com.ziheng.system.sysuser.domain.dto.SysUserAddDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserQuery;
import com.ziheng.system.sysuser.domain.dto.SysUserResetPasswordDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserUpdateProfileDTO;
import com.ziheng.system.sysuser.domain.dto.SysUserChangePasswordDTO;
import com.ziheng.system.sysuser.domain.vo.SysUserEditVO;
import com.ziheng.system.sysuser.domain.vo.SysUserVo;
import com.ziheng.system.sysuser.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Tag(name = "测试接口")
@RequestMapping("/system/user")
public class SysUserController {
    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    @Operation(summary = "用户查询")
    public R<PageResult<SysUserVo>> list(SysUserQuery query) {
        return R.ok(sysUserService.listUsers(query));
    }

    /**
     * 新增或修改用户（userId 为空表示新增，不为空表示修改）
     */
    @PostMapping
    @SaCheckPermission(value = {"system:user:add", "system:user:edit"}, mode = SaMode.OR)
    @Operation(summary = "新增或修改用户")
    @OperateLog(module = OperateModule.USER,idSource = IdSource.BODY,
            type = OperateType.INSERT_UPDATE, idField = "userId", service = SysUserService.class)
    public R<Long> add(@Valid @RequestBody SysUserAddDTO dto) {
        return R.ok(sysUserService.addUser(dto));
    }

    /**
     * 查询用户编辑信息（与新增/编辑提交字段一致，但不返回用户名与密码）
     */
    @GetMapping("/{userId}")
    @Operation(summary = "查询用户详情")
    public R<SysUserEditVO> info(@PathVariable("userId") Long userId) {
        return R.ok(sysUserService.getUserDetail(userId));
    }


    /**
     * 删除用户（支持单条或批量，逻辑删除）
     */
    @PostMapping("/delete")
    @SaCheckPermission("system:user:delete")
    @Operation(summary = "删除用户")
    @OperateLog(module = OperateModule.USER,idSource = IdSource.BODY,
            type = OperateType.DELETE, idField = "userIds", service = SysUserService.class)
    public R<Void> remove(@RequestBody List<Long> userIds) {
        sysUserService.deleteUsers(userIds);
        return R.ok();
    }

    /**
     * 重置用户密码（重新生成盐值并加密存储）
     */
    @PostMapping("/resetPassword")
    @SaCheckPermission("system:user:resetPwd")
    @Operation(summary = "重置用户密码")
    @OperateLog(module = OperateModule.USER,idSource = IdSource.BODY,
            type = OperateType.UPDATE, idField = "userId", service = SysUserService.class)
    public R<Void> resetPassword(@Valid @RequestBody SysUserResetPasswordDTO dto) {
        sysUserService.resetPassword(dto);
        return R.ok();
    }

    /**
     * 查询当前登录用户资料（个人中心）。
     *
     * <p>查的是登录态对应的用户，不需要额外菜单权限；
     * 最近登录从登录日志里取最新一条成功记录拼接展示。</p>
     */
    @SaCheckLogin
    @GetMapping("/profile")
    @Operation(summary = "查询当前用户资料")
    public R<SysUserProfileVO> profile() {
        return R.ok(sysUserService.getUserProfile(LoginUserUtils.getUserId()));
    }

    /**
     * 上传并更新当前登录用户头像。
     * 仅允许 jpg、png，成功后返回头像相对存储路径。
     */
    @SaCheckLogin
    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public R<String> avatar(@RequestParam("avatar") MultipartFile file) {
        return R.ok(sysUserService.updateAvatar(file));
    }

    /**
     * 修改当前登录用户个人信息（仅昵称、性别、备注）。
     */
    @SaCheckLogin
    @PostMapping("/profile/update")
    @Operation(summary = "修改个人信息")
    public R<Void> updateProfile(@Valid @RequestBody SysUserUpdateProfileDTO dto) {
        sysUserService.updateProfile(dto);
        return R.ok();
    }

    /**
     * 修改当前登录用户密码（需校验原密码，并使用新盐值重新加密）。
     */
    @SaCheckLogin
    @PostMapping("/changePassword")
    @Operation(summary = "修改密码")
    public R<Void> changePassword(@Valid @RequestBody SysUserChangePasswordDTO dto) {
        sysUserService.changePassword(dto);
        return R.ok();
    }

    @SaCheckLogin
    @GetMapping("/export")
    @SaCheckPermission("system:user:export")
    @Operation(summary = "导出用户")
    public void export(SysUserQuery query, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = "用户列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        sysUserService.export(query, response);
    }
}
