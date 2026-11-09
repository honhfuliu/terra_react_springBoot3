package com.ziheng.ai.infrastructure.client.factory;

import com.ziheng.ai.common.enums.AiModelType;
import com.ziheng.ai.common.enums.AiProtocolType;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.provider.service.AiProviderService;
import com.ziheng.ai.infrastructure.security.AiApiKeyEncryptor;
import com.ziheng.common.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * AI 聊天客户端工厂
 *
 * 根据数据库中的模型和提供商配置，动态创建 ChatClient
 */
@Component
public class AiChatClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiChatClientFactory.class);

    private final AiModelService aiModelService;
    private final AiProviderService aiProviderService;
    private final AiApiKeyEncryptor aiApiKeyEncryptor;

    public AiChatClientFactory(AiModelService aiModelService, AiProviderService aiProviderService, AiApiKeyEncryptor aiApiKeyEncryptor) {
        this.aiModelService = aiModelService;
        this.aiProviderService = aiProviderService;
        this.aiApiKeyEncryptor = aiApiKeyEncryptor;
    }

    /**
     * 获取当前可用的聊天模型
     */
    private AiModel getAvailableChatModel() {
        return aiModelService.getAvailableModel(AiModelType.CHAT.getCode());
    }

    /**
     * 获取模型所属提供商
     */
    private AiProvider getProvider(AiModel model) {
        AiProvider provider = aiProviderService.getById(model.getProviderId());
        if (provider == null) {
            throw new BusinessException("模型所属提供商不存在");
        }
        if (!"1".equals(provider.getEnabled())) {
            throw new BusinessException("模型所属提供商已禁用");
        }
        return provider;
    }

    /**
     * 获取解密后的 API Key
     */
    private String getApiKey(AiProvider provider) {
        String encryptedKey = provider.getApiKey();
        LOGGER.info("encryptedKey: {}", encryptedKey);
        if (encryptedKey == null || encryptedKey.isBlank()) {
            throw new BusinessException("提供商 API Key 未配置");
        }
        return aiApiKeyEncryptor.decrypt(encryptedKey);
    }


    /**
     * 创建动态聊天客户端
     *
     * @return ChatClient
     */
    public ChatClient create() {
        // 1. 获取当前可用的聊天模型
        AiModel model = getAvailableChatModel();
        // 2. 获取模型所属提供商
        AiProvider provider = getProvider(model);
        // 3. 校验协议类型
        if (!AiProtocolType.OPENAI_COMPATIBLE.getCode().equals(provider.getProtocol())) {
            throw new BusinessException("暂不支持该 AI 通信协议");
        }
        // 4. 解密 API Key
        String apiKey = getApiKey(provider);
        LOGGER.info("apiKey: {}", apiKey);
        // 5. 创建 OpenAiApi
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(provider.getApiBaseUrl())
                .apiKey(apiKey)
                .build();

        // 6. 配置模型参数
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder().model(model.getModelCode());

        if (model.getTemperature() != null) {
            optionsBuilder.temperature(model.getTemperature().doubleValue());
        }

        if (model.getMaxTokens() != null) {
            optionsBuilder.maxTokens(model.getMaxTokens());
        }
        // 7. 创建 OpenAiChatModel
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();

        // 8. 创建 ChatClient
        return ChatClient.builder(chatModel).build();
    }


}
