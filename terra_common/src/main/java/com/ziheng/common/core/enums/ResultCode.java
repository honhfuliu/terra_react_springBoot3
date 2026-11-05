package com.ziheng.common.core.enums;

public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "认证失败"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统异常"),
    RC5001(5001, "测试业务异常"),
    USER_NOT_FOUND(4001, "用户不存在"),
    PASSWORD_ERROR(4002, "密码错误"),
    CAPTCHA_ERROR(4003, "验证码错误"),
    USER_DISABLED(4004, "用户已被禁用");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
