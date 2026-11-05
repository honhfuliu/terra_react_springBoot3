package com.ziheng.system.security.util;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 当前登录用户工具（基于 Sa-Token）。
 *
 * <p>登录时只往 Token 里塞了 userId，操作日志还要记"操作账号"，
 * 与其每次记日志都回查一次 sys_user，不如登录时把用户名一并挂到 Token-Session 上，
 * 这里统一读取：取不到（未登录 / 老 Token）返回 {@code null}，不抛异常。</p>
 *
 * <p>挂在 Token-Session 而不是 User-Session（{@code StpUtil.getSession()}），
 * 是因为前者随 Token 注销一起销毁，语义上就是"这一次登录"。</p>
 */
public final class LoginUserUtils {

    /**
     * 用户名在 Token-Session 中的 key，登录时写入（见 AuthServiceImpl）。
     */
    public static final String KEY_USERNAME = "username";

    private LoginUserUtils() {
    }

    /**
     * 当前登录用户 ID；未登录返回 {@code null}。
     */
    public static Long getUserId() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 当前登录账号；未登录或 Token 中没有该信息时返回 {@code null}。
     */
    public static String getUsername() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        Object username = StpUtil.getTokenSession().get(KEY_USERNAME);
        return username == null ? null : String.valueOf(username);
    }
}
