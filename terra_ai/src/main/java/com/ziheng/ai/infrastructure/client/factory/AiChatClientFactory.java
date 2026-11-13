package com.ziheng.ai.infrastructure.client.factory;

import com.ziheng.ai.common.enums.AiModelType;
import com.ziheng.ai.common.enums.AiProtocolType;
import com.ziheng.ai.infrastructure.client.context.AiChatContext;
import com.ziheng.ai.infrastructure.client.context.AiModelContext;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.security.AiApiKeyEncryptor;
import com.ziheng.common.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
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
    private final AiApiKeyEncryptor aiApiKeyEncryptor;

    public AiChatClientFactory(AiModelService aiModelService, AiApiKeyEncryptor aiApiKeyEncryptor) {
        this.aiModelService = aiModelService;
        this.aiApiKeyEncryptor = aiApiKeyEncryptor;
    }


    /**
     * 获取解密后的 API Key
     */
    private String getApiKey(AiProvider provider) {
        String encryptedKey = provider.getApiKey();
        // LOGGER.info("encryptedKey: {}", encryptedKey);
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
    private AiChatContext create(AiModelContext context) {
        String protocol = context.getProvider().getProtocol();
        return switch (protocol) {
            case "OPENAI_COMPATIBLE" -> createOpenAiCompatible(context);
            case "OLLAMA" -> createOllama(context);
            default -> throw new BusinessException("暂不支持该 AI 通信协议");
        };
    }
    /**
     * 创建 Ollama 聊天客户端
     * @param context 模型上下文
     * @return AI 聊天上下文
     */
    private AiChatContext createOllama(AiModelContext context) {
        AiModel model = context.getModel();
        AiProvider provider = context.getProvider();

        // 创建 OllamaApi
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(provider.getApiBaseUrl())
                .build();

        // 配置 OllamaOptions
        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder()
                .model(model.getModelCode());
        if (model.getTemperature() != null) {
            optionsBuilder.temperature(model.getTemperature().doubleValue());
        }

        if (model.getMaxTokens() != null) {
            optionsBuilder.numPredict(model.getMaxTokens());
        }
        // 创建 OllamaChatModel
        OllamaChatModel chatModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(optionsBuilder.build())
                .build();

        // 创建统一 ChatClient
        return new AiChatContext(ChatClient.builder(chatModel).build(), model, provider);
    }

    /**
     * 创建 OpenAI 兼容的聊天客户端
     * @param context 模型上下文
     * @return AI 聊天上下文
     */
    private AiChatContext createOpenAiCompatible(AiModelContext context) {
        AiModel model = context.getModel();
        AiProvider provider = context.getProvider();
        // 解密 API Key
        String apiKey = getApiKey(provider);
        // 创建 OpenAiApi
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(provider.getApiBaseUrl())
                .apiKey(apiKey)
                .build();

        // 配置模型参数
        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(model.getModelCode())
                .streamUsage(true);

        if (model.getTemperature() != null) {
            optionsBuilder.temperature(model.getTemperature().doubleValue());
        }

        if (model.getMaxTokens() != null) {
            optionsBuilder.maxTokens(model.getMaxTokens());
        }
        // 创建 OpenAiChatModel
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();

        // 创建 ChatClient并返回
        return new AiChatContext(ChatClient.builder(chatModel).build(), model, provider);
    }
    /**
     * 根据模型ID创建客户端
     */
    public AiChatContext create(Long modelId) {
        AiModelContext context = aiModelService.getAvailableModelContext(AiModelType.CHAT.getCode(), modelId);
        return create(context);
    }

}
