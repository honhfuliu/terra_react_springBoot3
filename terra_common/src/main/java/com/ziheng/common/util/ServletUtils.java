package com.ziheng.common.util;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求信息工具类：统一获取当前请求、客户端 IP、浏览器与操作系统。
 *
 * <p>凡是"从 HttpServletRequest 里取信息"的需求都走这里，业务代码（切面、拦截器、Service）
 * 不再各自去 new Hutool 调用或裸用 {@code RequestContextHolder}，
 * 免得同一件事在不同文件里出现好几种实现。</p>
 *
 * <pre>
 *   HttpServletRequest request = ServletUtils.getRequest();
 *   String ip       = ServletUtils.getClientIp(request);
 *   String location = ServletUtils.getIpLocation(ip);
 *   String browser  = ServletUtils.getBrowser(request);
 *   String os       = ServletUtils.getOs(request);
 * </pre>
 *
 * <p>约定：所有方法在非 Web 线程（定时任务、{@code @Async}、单元测试）下返回 {@code null}
 * 或 {@link #UNKNOWN}，不抛异常——工具方法不应该让调用方到处 try-catch。</p>
 *
 * <p>依赖说明：本类用到 {@code spring-web} 与 {@code jakarta.servlet-api}，
 * 在 terra_common 的 pom 中分别声明为 {@code optional} 与 {@code provided}，
 * 运行时的 servlet 实现由容器（Tomcat）提供。</p>
 */
public final class ServletUtils {

    /** 未知：IP 为空、UA 解析不出浏览器/系统时的占位符 */
    public static final String UNKNOWN = "未知";

    /** 内网 */
    public static final String INTRANET = "内网";

    private static final String HEADER_USER_AGENT = "User-Agent";

    private ServletUtils() {
    }

    // ==================== 请求上下文 ====================

    /**
     * 获取当前请求；非 Web 线程返回 {@code null}。
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    /**
     * 当前线程是否处于 Web 请求中。
     */
    public static boolean isWebRequest() {
        return getRequest() != null;
    }

    // ==================== IP ====================

    /**
     * 获取客户端 IP；非 Web 线程返回 {@code null}。
     */
    public static String getClientIp() {
        return getClientIp(getRequest());
    }

    /**
     * 获取客户端 IP。
     *
     * <p>走 Hutool 的实现，它已经处理好 X-Forwarded-For、X-Real-IP 等反向代理头，
     * 不要自己解析 X-Forwarded-For（取值位置、多级代理怎么截断，很容易写错）。</p>
     *
     * <p>注意：这些请求头客户端可以伪造，用于日志展示没问题；
     * 若要用它做安全校验，必须配合可信代理（如 Nginx 覆写而非追加）来使用。</p>
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return JakartaServletUtil.getClientIP(request);
    }

    /**
     * 解析 IP 归属地。
     *
     * <p>项目尚未接入 IP 归属地库，因此只能区分内网与外网，返回 {@link #UNKNOWN}
     * 而不是编造省市信息；将来接入离线库（如 ip2region）只需改这一个方法。</p>
     */
    public static String getIpLocation(String ip) {
        if (StrUtil.isBlank(ip)) {
            return UNKNOWN;
        }
        return isInnerIp(ip) ? INTRANET : UNKNOWN;
    }

    /**
     * 是否内网 IP（IPv4 私有网段 / IPv6 本地地址）。
     *
     * <p>注意：Hutool 的 {@code NetUtil.isInnerIP} 只认 IPv4，
     * 传入 IPv6（本机访问时拿到的是 {@code ::1}）会直接抛 {@code IllegalArgumentException}，
     * 所以这里按 IP 版本分流。</p>
     */
    public static boolean isInnerIp(String ip) {
        if (StrUtil.isBlank(ip)) {
            return false;
        }
        return ip.indexOf(':') >= 0 ? isInnerIpv6(ip) : NetUtil.isInnerIP(ip);
    }

    /**
     * 判断 IPv6 是否属于本地/内网地址。
     */
    private static boolean isInnerIpv6(String ip) {
        String value = ip.toLowerCase();
        // 回环地址 ::1（某些容器网络下会拿到完整写法 0:0:0:0:0:0:0:1）
        if ("::1".equals(value) || "0:0:0:0:0:0:0:1".equals(value)) {
            return true;
        }
        // fc00::/7（唯一本地地址，fc、fd 开头）
        // fe80::/10（链路本地地址，fe8、fe9、fea、feb 开头）
        return value.startsWith("fc") || value.startsWith("fd")
                || value.startsWith("fe8") || value.startsWith("fe9")
                || value.startsWith("fea") || value.startsWith("feb");
    }

    // ==================== User-Agent ====================

    /**
     * 获取原始 User-Agent；非 Web 线程返回 {@code null}。
     */
    public static String getUserAgent() {
        return getUserAgent(getRequest());
    }

    /**
     * 获取原始 User-Agent。
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request == null ? null : request.getHeader(HEADER_USER_AGENT);
    }

    /**
     * 解析 User-Agent；入参为空时返回 {@code null}。
     */
    public static UserAgent parseUserAgent(String userAgentString) {
        return StrUtil.isBlank(userAgentString) ? null : UserAgentUtil.parse(userAgentString);
    }

    /**
     * 获取浏览器名称（如 Chrome、Edge）；取不到返回 {@link #UNKNOWN}。
     */
    public static String getBrowser() {
        return getBrowser(getRequest());
    }

    public static String getBrowser(HttpServletRequest request) {
        return getBrowser(getUserAgent(request));
    }

    public static String getBrowser(String userAgentString) {
        UserAgent userAgent = parseUserAgent(userAgentString);
        return userAgent == null || userAgent.getBrowser() == null
                ? UNKNOWN : userAgent.getBrowser().getName();
    }

    /**
     * 获取操作系统名称（如 Windows 10、Linux）；取不到返回 {@link #UNKNOWN}。
     */
    public static String getOs() {
        return getOs(getRequest());
    }

    public static String getOs(HttpServletRequest request) {
        return getOs(getUserAgent(request));
    }

    public static String getOs(String userAgentString) {
        UserAgent userAgent = parseUserAgent(userAgentString);
        return userAgent == null || userAgent.getOs() == null
                ? UNKNOWN : userAgent.getOs().getName();
    }
}
