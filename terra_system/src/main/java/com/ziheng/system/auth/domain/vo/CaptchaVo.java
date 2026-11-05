package com.ziheng.system.auth.domain.vo;

import lombok.Data;

/**
 * 图形验证码返回对象。
 * 后端不直接返回图片流，而是返回 base64，
 * 这样前端可以在同一个 JSON 响应里同时拿到 uuid 和图片，避免跨域、Cookie、缓存等额外问题。</p>
 */
@Data
public class CaptchaVo {

    /**
     * 是否启用验证码。
     * 关闭时（terra.captcha.enabled = false）为 false，此时 uuid、img 都为空，
     * 前端据此隐藏验证码输入框。
     */
    private boolean enabled;

    /**
     * 验证码唯一标识，登录时需要原样回传。
     * 相当于"这次验证码答案存在 Redis 哪个 key 下"的索引，
     * 本身不含任何答案信息，可以安全暴露给前端。
     */
    private String uuid;

    /**
     * 验证码图片的 base64 内容，形如 data:image/png;base64,xxx。
     * 前端可以直接用于 img 标签的 src：{@code <img :src="captcha.img" />}
     */
    private String img;
}
