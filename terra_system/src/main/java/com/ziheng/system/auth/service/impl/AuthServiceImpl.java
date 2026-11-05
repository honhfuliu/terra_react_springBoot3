package com.ziheng.system.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.common.core.enums.ResultCode;
import com.ziheng.common.util.PasswordUtils;
import com.ziheng.system.auth.domain.dto.LoginBody;
import com.ziheng.system.auth.domain.vo.LoginVo;
import com.ziheng.system.auth.service.AuthService;
import com.ziheng.system.auth.service.CaptchaService;
import com.ziheng.system.security.util.LoginUserUtils;
import com.ziheng.system.sysuser.domain.SysUser;
import com.ziheng.system.sysuser.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserMapper sysUserMapper;
    private final CaptchaService captchaService;

    public AuthServiceImpl(SysUserMapper sysUserMapper, CaptchaService captchaService) {
        this.sysUserMapper = sysUserMapper;
        this.captchaService = captchaService;
    }

    /**
     * 用户登录
     * @param loginBody
     * @return
     */
    @Override
    public LoginVo login(LoginBody loginBody) {

        // 先校验验证码，再查库比对密码。
        // 放在最前面有两个好处：验证码不通过就不必访问数据库；
        // 同时挡住了绕过前端直接拿脚本撞库的请求
        captchaService.validate(loginBody.getUuid(), loginBody.getCode());

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDelFlag, "0")
                        .eq(SysUser::getUsername, loginBody.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }

        if ("0".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_DISABLED.getCode(), "用户已被禁用");
        }

        if (!PasswordUtils.matches(loginBody.getPassword(), user.getPassword(), user.getSalt())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(), "密码错误");
        }

        StpUtil.login(user.getUserId());
        // 登录态里补一份用户名：操作日志要记"操作账号"，
        // 存这里比每次记日志都回查 sys_user 划算，Token 注销后随会话一起销毁
        StpUtil.getTokenSession().set(LoginUserUtils.KEY_USERNAME, user.getUsername());

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(StpUtil.getTokenValue());
        loginVo.setUserId(user.getUserId());
        loginVo.setUsername(user.getUsername());
        loginVo.setNickname(user.getNickname());
        return loginVo;
    }
}
