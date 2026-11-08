package com.ziheng.ai.infrastructure.provider.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI模型提供商新增/修改参数
 */
@Data
public class AiProviderSaveDTO {

    /**
     * 主键（修改时必传，新增时为空）
     */
    private Long id;

    /**
     * 提供商编码
     */
    @NotBlank(message = "提供商编码不能为空")
    private String providerCode;

    /**
     * 提供商名称
     */
    @NotBlank(message = "提供商名称不能为空")
    private String providerName;

    /**
     * API地址
     */
    @NotBlank(message = "API地址不能为空")
    private String apiBaseUrl;

    /**
     * 加密后的API Key
     */
    private String apiKey;

    /**
     * 接入协议
     */
    @NotBlank(message = "接入协议不能为空")
    private String protocol;

    /**
     * 是否启用 0否 1是
     */
    private String enabled;

    /**
     * 是否设为默认 0否 1是（传1时先取消原有默认提供商；修改时传0可取消当前默认）
     */
    private String isDefault;

    /**
     * 备注
     */
    private String remark;
}
