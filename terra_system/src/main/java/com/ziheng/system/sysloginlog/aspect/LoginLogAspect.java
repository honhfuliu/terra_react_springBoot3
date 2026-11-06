package com.ziheng.system.sysloginlog.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.ziheng.common.core.domain.R;
import com.ziheng.common.core.enums.ExecuteStatus;
import com.ziheng.common.util.ServletUtils;
import com.ziheng.system.auth.domain.dto.LoginBody;
import com.ziheng.system.auth.domain.vo.LoginVo;
import com.ziheng.system.sysloginlog.annotation.LoginLog;
import com.ziheng.system.sysloginlog.domain.SysLoginLog;
import com.ziheng.system.sysloginlog.service.SysLoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 登录日志切面。
 *
 * <p>环绕标记了 {@link LoginLog} 的方法：</p>
 * <pre>
 *   /auth/login  ──►  @LoginLog  ──►  LoginLogAspect  ──┬─► 正常返回：status=1 登录成功
 *                                                       └─► 抛异常：status=0 登录失败
 *                                                                  │
 *                                                                  ▼
 *                                                        SysLoginLogService.save()  ──►  sys_login_log
 * </pre>
 *
 * <h3>几个关键设计点</h3>
 * <ol>
 *   <li><b>失败也要留痕</b>：账号、IP、浏览器等在 {@code proceed()} 之前就采集好，
 *       因为业务抛异常后拿不到这些信息了。</li>
 *   <li><b>异常必须继续抛出</b>：登录失败靠 {@code GlobalExceptionHandler} 转成前端提示，
 *       切面吞掉异常会导致"密码错了却返回 200"。</li>
 *   <li><b>日志不能拖垮登录</b>：落库异常只打 warn，不影响登录本身。</li>
 *   <li><b>不依赖 Sa-Token 上下文</b>：登录成功前 / 失败时 {@code StpUtil.getLoginId()}
 *       会抛未登录异常，所以账号从请求参数取、userId 从返回值取。</li>
 * </ol>
 */
@Aspect
@Component
public class LoginLogAspect {

    private static final Logger log = LoggerFactory.getLogger(LoginLogAspect.class);

    private final SysLoginLogService sysLoginLogService;

    public LoginLogAspect(SysLoginLogService sysLoginLogService) {
        this.sysLoginLogService = sysLoginLogService;
    }

    /**
     * 环绕通知：拦截所有标注了 {@link LoginLog} 的方法。
     *
     * <p>切点写注解全限定名、注解实例从方法签名上取，不走 AOP 的参数名绑定：
     * 参数名绑定依赖编译期保留调试信息，换编译器或打包方式时容易失败。</p>
     */
    @Around("@annotation(com.ziheng.system.sysloginlog.annotation.LoginLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        LoginLog loginLog = resolveAnnotation(point);

        SysLoginLog entity = new SysLoginLog();
        // ① 先行采集请求侧信息，保证失败场景也有据可查
        fillRequestInfo(entity, point);
        entity.setLoginType(loginLog == null ? "" : loginLog.loginType());

        try {
            Object result = point.proceed();
            // ② 正常返回即登录成功
            entity.setStatus(ExecuteStatus.SUCCESS.getCode());
            entity.setMessage("登录成功");
            fillUserIdOnSuccess(entity, result);
            return result;
        } catch (Throwable e) {
            // ③ 业务异常（密码错误、账号停用、验证码错误等）记为登录失败
            entity.setStatus(ExecuteStatus.FAIL.getCode());
            entity.setMessage(StrUtil.blankToDefault(e.getMessage(), e.getClass().getSimpleName()));
            // 继续抛出，交给 GlobalExceptionHandler 统一返回给前端
            throw e;
        } finally {
            // ④ 成功与失败都要落库，时间统一在最后取
            entity.setLoginTime(new Date());
            entity.setCreateTime(entity.getLoginTime());
            saveQuietly(entity);
        }
    }

    /**
     * 从被拦截的方法签名上取 @LoginLog 注解。
     */
    private LoginLog resolveAnnotation(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        return AnnotatedElementUtils.findMergedAnnotation(signature.getMethod(), LoginLog.class);
    }

    /**
     * 采集账号、IP、浏览器、操作系统等信息。
     */
    private void fillRequestInfo(SysLoginLog entity, ProceedingJoinPoint point) {
        // 账号：遍历参数找 LoginBody，而不是写死 args[0]，换 LoginBody 位置也不用改切面
        for (Object arg : point.getArgs()) {
            if (arg instanceof LoginBody loginBody) {
                entity.setUsername(loginBody.getUsername());
                break;
            }
        }
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            // 非 Web 线程调用（定时任务、单元测试）没有请求上下文，跳过
            return;
        }
        // IP 与归属地
        String ip = ServletUtils.getClientIp(request);
        entity.setIpAddress(StrUtil.blankToDefault(ip, ""));
        entity.setLoginLocation(ServletUtils.getIpLocation(ip));

        // User-Agent：原始串留一份便于排查，同时解析出浏览器与操作系统
        String userAgent = ServletUtils.getUserAgent(request);
        // 超长截断，避免超出 sys_login_log.user_agent 列宽度导致写入失败
        entity.setUserAgent(userAgent);
        entity.setBrowser(ServletUtils.getBrowser(userAgent));
        entity.setOs(ServletUtils.getOs(userAgent));
    }

    /**
     * 登录成功时补充 userId。
     */
    private void fillUserIdOnSuccess(SysLoginLog entity, Object result) {
        if (result instanceof R<?> r && r.getData() instanceof LoginVo loginVo) {
            entity.setUserId(loginVo.getUserId());
            // 兜底：万一以后换了入参类型取不到账号，用返回结果里的账号补上
            if (StrUtil.isBlank(entity.getUsername())) {
                entity.setUsername(loginVo.getUsername());
            }
            // 获取登录成功时生成的 Token
            String token = loginVo.getToken();
            // 保存 Token 的 SHA-256 哈希值，不保存原始 Token
            if (StrUtil.isNotBlank(token)) {
                entity.setTokenHash(SecureUtil.sha256(token));
            }
        }
    }

    /**
     * 写库并兜住异常：登录日志属于旁路信息，写失败不能影响登录主流程。
     */
    private void saveQuietly(SysLoginLog entity) {
        try {
            sysLoginLogService.save(entity);
        } catch (Exception e) {
            log.warn("登录日志写入失败，username={}，原因={}", entity.getUsername(), e.getMessage());
        }
    }
}
