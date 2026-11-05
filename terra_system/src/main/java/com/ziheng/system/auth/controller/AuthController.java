package com.ziheng.system.auth.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.ziheng.system.sysloginlog.annotation.LoginLog;
import com.ziheng.common.core.domain.R;
import com.ziheng.system.auth.domain.dto.LoginBody;
import com.ziheng.system.auth.domain.vo.CaptchaVo;
import com.ziheng.system.auth.domain.vo.LoginVo;
import com.ziheng.system.auth.domain.vo.UserPermissionVO;
import com.ziheng.system.auth.service.AuthService;
import com.ziheng.system.auth.service.CaptchaService;
import com.ziheng.system.sysmenu.domain.SysMenu;
import com.ziheng.system.sysmenu.domain.vo.RouterVO;
import com.ziheng.system.sysmenu.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final SysMenuService sysMenuService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService,  SysMenuService sysMenuService, CaptchaService captchaService) {
        this.authService = authService;
        this.sysMenuService =sysMenuService;
        this.captchaService = captchaService;
    }
    @GetMapping("getRouters")
    @Operation(summary = "获取动态路由")
    public R<RouterVO> getRouters(){
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("userId: {}", userId);
        List<SysMenu> sysMenuVOS = sysMenuService.selectMenuTreeByUserId(userId);
        return R.ok(sysMenuService.convertToRouteTree(sysMenuVOS));
    }
    @GetMapping("permissions")
    @Operation(summary = "获取权限")
    @SaCheckLogin()
    public R<UserPermissionVO> getPermissions(){
        List<String> permissions = StpUtil.getPermissionList();
        List<String> roleList = StpUtil.getRoleList();
        UserPermissionVO userPermissionVO = new UserPermissionVO(permissions,roleList);
        return R.ok(userPermissionVO);
    }

    /**
     * 获取图形验证码。
     *
     * <p>返回 uuid 与图片 base64：前端把图片显示出来，
     * 登录时把 uuid 与用户输入的验证码一起回传，
     * 后端据此从 Redis 取出答案比对。</p>
     *
     * <p>该接口必须允许匿名访问，放行规则见 SaTokenConfig。</p>
     */
    @GetMapping("getCode")
    @Operation(summary = "获取验证码")
    public R<CaptchaVo> getCode() {
        return R.ok(captchaService.generate());
    }


    @PostMapping("login")
    @Operation(summary = "用户登录")
    @LoginLog(loginType = "账号密码登录")
    public R<LoginVo> login(@Valid @RequestBody LoginBody loginBody) {
        return R.ok(authService.login(loginBody));
    }


    @PostMapping("logout")
    @Operation(summary = "退出登录")
    public R<Void> logout() {
        StpUtil.logout();
        return R.ok();
    }
}
