package com.ziheng.system.auth.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ziheng.common.core.enums.ResultCode;
import com.ziheng.common.exception.BusinessException;
import com.ziheng.system.auth.domain.vo.CaptchaVo;
import com.ziheng.system.auth.service.CaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 图形验证码服务实现：Hutool 负责画图，Redis 负责存答案。
 *
 * <h3>为什么答案必须放在 Redis</h3>
 * <ol>
 *   <li>服务是无状态的：登录请求不保证落在生成验证码的那台机器上，放本地内存会导致多实例下校验失败；</li>
 *   <li>重启不丢：存在内存里，服务一重启所有人都要重新获取验证码；</li>
 *   <li>自动过期：Redis 的 TTL 天然解决"验证码 2 分钟后失效"，不需要自己写定时清理；</li>
 *   <li>一次性消费：校验后立刻删除，防止同一个验证码被反复提交用于撞库。</li>
 * </ol>
 *
 * <h3>为什么用 StringRedisTemplate 而不是上一轮配置的 redisTemplate</h3>
 * <p>验证码的答案就是一个普通字符串，用 StringRedisTemplate 存取最直接，
 * 也不会被写入类型信息（{@code @class}）、不会在 value 里多出一对引号。
 * 自定义的 redisTemplate 适合缓存"对象"，简单值走 String 更合适。</p>
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * Redis key 前缀。
     * 与 Sa-Token 的 satoken: 前缀区分开，避免两类数据混在一起不好排查。
     */
    private static final String CAPTCHA_KEY_PREFIX = "terra:captcha:";

    /** 图片宽度（像素） */
    private static final int WIDTH = 100;

    /** 图片高度（像素） */
    private static final int HEIGHT = 30;

    /** 验证码位数 */
    private static final int CODE_LENGTH = 4;

    /** 干扰线数量：过多会让人眼也难识别，30 左右比较平衡 */
    private static final int LINE_COUNT = 30;

    /**
     * 验证码字符集。
     * 刻意去掉了 0/O、1/I/l 这类容易看错的字符，
     * 减少"明明输对了却提示验证码错误"的情况。
     */
    private static final String CODE_CHARS = "23456789abcdefghjkmnpqrstuvwxyz";

    /**
     * 有效期，前端倒计时建议与此保持一致。
     * 时间不宜过长，否则会给验证码撞库留出更多窗口。
     */
    private static final Duration EXPIRE = Duration.ofMinutes(2);

    /** Redis 客户端，只用字符串序列化，存验证码答案最合适 */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 验证码开关，对应配置项 terra.captcha.enabled，默认开启。
     * 本地联调、接口压测时可以临时关掉，省去每次输入验证码的麻烦。
     */
    @Value("${terra.captcha.enabled:true}")
    private boolean enabled;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public CaptchaVo generate() {

        CaptchaVo vo = new CaptchaVo();
        vo.setEnabled(enabled);

        // 开关关闭时既不生成图片也不写 Redis，前端拿到 enabled=false 会隐藏验证码输入框
        if (!enabled) {
            return vo;
        }

        // 生成带干扰线的验证码图片。
        // 字符集与位数由 RandomGenerator 控制，比默认字符集更易辨认。
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(
                WIDTH,
                HEIGHT,
                new RandomGenerator(CODE_CHARS, CODE_LENGTH),
                LINE_COUNT
        );

        // uuid 只是"索引"，不含答案，因此可以安全地返回给前端
        String uuid = IdUtil.fastSimpleUUID();
        String code = captcha.getCode();

        // 答案写入 Redis，2 分钟后由 Redis 自动删除，无需自己清理
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_KEY_PREFIX + uuid,
                code,
                EXPIRE
        );

        vo.setUuid(uuid);
        // getImageBase64Data() 返回的是 data:image/png;base64,xxx，前端可直接用作 img 的 src
        vo.setImg(captcha.getImageBase64Data());

        return vo;
    }

    @Override
    public void validate(String uuid, String code) {
        // 开关关闭时直接放行，登录流程不受影响
        if (!enabled) {
            return;
        }
        if (StrUtil.isBlank(uuid) || StrUtil.isBlank(code)) {
            throw new BusinessException(
                    ResultCode.BAD_REQUEST.getCode(),
                    "验证码不能为空"
            );
        }

        String key = CAPTCHA_KEY_PREFIX + uuid;
        String cached = stringRedisTemplate.opsForValue().get(key);

        /*
         * 先删除再比对，保证验证码只能使用一次。
         *
         * 如果改成"比对成功后再删除"，同一个 uuid 就可以被无限次提交，
         * 攻击者相当于绕过了验证码，可以放心地暴力尝试密码。
         */
        stringRedisTemplate.delete(key);

        if (StrUtil.isBlank(cached)) {
            throw new BusinessException(
                    ResultCode.BAD_REQUEST.getCode(),
                    "验证码已过期，请重新获取"
            );
        }

        // 忽略大小写：用户不必纠结大小写，同时不放宽任何有效范围
        if (!StrUtil.equalsIgnoreCase(cached, code)) {
            throw new BusinessException(
                    ResultCode.CAPTCHA_ERROR.getCode(),
                    "验证码错误"
            );
        }
    }
}
