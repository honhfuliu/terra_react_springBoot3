package com.ziheng.ai.infrastructure.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * AI模型提供商表
 * @TableName ai_provider
 */
@TableName(value ="ai_provider")
@Data
public class AiProvider {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
     * 加密后的API Key
     */
    private String apiKey;

    /**
     * 接入协议
     */
    private String protocol;

    /**
     * 是否启用 0否 1是
     */
    private String enabled;

    /**
     * 是否默认 0否 1是
     */
    private String isDefault;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;


}
