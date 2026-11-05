package com.ziheng.system.auth.service;

import com.ziheng.system.auth.domain.vo.CaptchaVo;

/**
 * 图形验证码服务。
 * 答案保存在 Redis 中，key 由 uuid 索引，因此：
 * 服务可以多实例部署、重启不丢、过期自动清理，验证码也只能被消费一次。</p>
 */
public interface CaptchaService {

    /**
     * 生成验证码。
     * 生成图片的同时把答案写入 Redis（有效期 2 分钟），
     * 返回 uuid 供登录时回传使用。</p>
     * @return 验证码信息，包含开关状态、uuid 与图片 base64
     */
    CaptchaVo generate();

    /**
     * 校验验证码，校验后立即删除，保证一次性使用。
     * @param uuid 生成验证码时返回的唯一标识
     * @param code 用户输入的验证码内容
     * @throws com.ziheng.common.exception.BusinessException 验证码为空、已过期或错误时抛出
     */
    void validate(String uuid, String code);
}
