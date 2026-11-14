package com.ziheng.framework.config;

import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import lombok.Data;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
public class DruidMonitorConfig {

    /**
     * 监控页登录账号
     */
    private String loginUsername = "admin";

    /**
     * 监控页登录密码
     */
    private String loginPassword = "admin123";

    /**
     * 允许访问的IP，多个用逗号分隔，为空表示不限制
     */
    private String allow = "";

    /**
     * 拒绝访问的IP，多个用逗号分隔
     */
    private String deny = "";

    /**
     * Druid 管理页面
     * 关键：setLoadOnStartup(1) 确保 Servlet 在静态资源处理器之前注册
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> druidStatViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");

        Map<String, String> initParams = new HashMap<>();
        initParams.put("loginUsername", defaultString(loginUsername, "admin"));
        initParams.put("loginPassword", defaultString(loginPassword, "admin123"));
        initParams.put("allow", defaultString(allow, ""));
        initParams.put("deny", defaultString(deny, ""));
        // 关闭监控页的 Reset All 功能，防止清空监控数据
        initParams.put("resetEnable", "false");
        bean.setInitParameters(initParams);
        bean.setName("druidStatViewServlet");
        // 必须早于静态资源处理器注册，否则 /druid/* 会被静态资源拦截
        bean.setLoadOnStartup(1);
        return bean;
    }

    /**
     * Druid Web 请求监控
     * 关键：exclusions 必须包含 /druid/*，否则 Filter 会拦截自己的 Servlet
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean =
                new FilterRegistrationBean<>(new WebStatFilter());
        bean.addUrlPatterns("/*");
        bean.addInitParameter("exclusions",
                "*.js,*.css,*.gif,*.jpg,*.jpeg,*.png,*.ico,*.woff2,/druid/*");
        bean.addInitParameter("sessionStatEnable", "true");
        bean.addInitParameter("sessionStatMaxCount", "1000");
        bean.setName("druidWebStatFilter");
        return bean;
    }

    /**
     * 配置写成 allow: / deny: 这类空值时，YAML 会绑定为 null，这里统一兜底
     */
    private String defaultString(String value, String defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : value;
    }

}
