package com.ziheng.system.sysloginlog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录日志注解。
 *
 * <p>标在登录方法上，由 {@code LoginLogAspect} 环绕增强：
 * 方法正常返回记为“成功”，抛出异常记为“失败”，
 * 并把账号、IP、浏览器、操作系统等信息写入 sys_login_log。</p>
 *
 * <h3>使用示例</h3>
 * <pre>
 * &#64;LoginLog(loginType = "账号密码登录")
 * &#64;PostMapping("login")
 * public R&lt;LoginVo&gt; login(&#64;Valid &#64;RequestBody LoginBody loginBody) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginLog {

    /**
     * 登录方式，写入 sys_login_log.login_type。
     * 默认值适用于账号密码登录，其他登录方式可自行指定。
     */
    String loginType() default "账号密码登录";
}
