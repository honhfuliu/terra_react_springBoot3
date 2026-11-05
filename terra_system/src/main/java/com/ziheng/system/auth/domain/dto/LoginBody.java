package com.ziheng.system.auth.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginBody {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码答案，由用户输入。
     *
     * <p>这里刻意不加 @NotBlank：验证码可以通过 terra.captcha.enabled 关闭，
     * 关闭时前端不会提交该字段，如果加了非空校验，关掉开关后反而无法登录。
     * 是否必填由 CaptchaService.validate() 内部依据开关状态判断。</p>
     */
    private String code;

    /**
     * 验证码唯一标识，来自 /auth/getCode 的返回，与 code 配对使用。
     */
    private String uuid;

}
