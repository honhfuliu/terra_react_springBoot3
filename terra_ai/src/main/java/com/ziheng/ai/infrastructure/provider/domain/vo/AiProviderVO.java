package com.ziheng.ai.infrastructure.provider.domain.vo;

import lombok.Data;

/**
 * AI模型提供商列表/详情视图对象
 */
@Data
public class AiProviderVO {

    /**
     * 主键（操作列使用）
     */
    private Long id;

    /**
     * 提供商编码
     */
    private String providerCode;

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * API地址
     */
    private String apiBaseUrl;

    /**
     * 接入协议
     */
    private String protocol;

    /**
     * 解密后脱敏的API Key
     */
    private String apiKey;

    /**
     * 状态 0停用 1启用
     */
    private String enabled;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否默认 0否 1是
     */
    private String isDefault;
}
