package com.ziheng.ai.infrastructure.client.factory;

import com.ziheng.ai.common.enums.AiModelType;
import com.ziheng.ai.infrastructure.client.context.AiEmbeddingContext;
import com.ziheng.ai.infrastructure.client.context.AiModelContext;
import com.ziheng.ai.infrastructure.model.domain.AiModel;
import com.ziheng.ai.infrastructure.model.service.AiModelService;
import com.ziheng.ai.infrastructure.provider.domain.AiProvider;
import com.ziheng.ai.infrastructure.security.AiApiKeyEncryptor;
import com.ziheng.common.exception.BusinessException;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * AI Embedding 客户端工厂
 */
@Component
public class AiEmbeddingClientFactory {
    private final AiModelService aiModelService;
    private final AiApiKeyEncryptor aiApiKeyEncryptor;

    public AiEmbeddingClientFactory(AiModelService aiModelService, AiApiKeyEncryptor aiApiKeyEncryptor) {
        this.aiModelService = aiModelService;
        this.aiApiKeyEncryptor = aiApiKeyEncryptor;
    }

    /**
     * 获取解密后的 API Key
     */
    private String getApiKey(AiProvider provider) {
        String encryptedKey = provider.getApiKey();

        if (encryptedKey == null || encryptedKey.isBlank()) {
            throw new BusinessException("提供商 API Key 未配置");
        }

        return aiApiKeyEncryptor.decrypt(encryptedKey);
    }

    /**
     * 根据协议创建 EmbeddingModel
     */
    private EmbeddingModel create(AiModelContext context, Integer dimension) {
        String protocol = context.getProvider().getProtocol();

        return switch (protocol) {
            case "OPENAI_COMPATIBLE" -> createOpenAiCompatible(context, dimension);
            case "OLLAMA" -> createOllama(context, dimension);
            default -> throw new BusinessException("暂不支持该 AI 通信协议");
        };
    }

    /**
     * 创建 OpenAI 兼容 Embedding 模型
     */
    private EmbeddingModel createOpenAiCompatible(AiModelContext context, Integer dimension) {
        AiModel model = context.getModel();
        AiProvider provider = context.getProvider();

        String apiKey = getApiKey(provider);

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(provider.getApiBaseUrl())
                .apiKey(apiKey)
                .build();

        OpenAiEmbeddingOptions.Builder optionsBuilder =
                OpenAiEmbeddingOptions.builder()
                        .dimensions(dimension)
                        .model(model.getModelCode());

        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(
                openAiApi,
                MetadataMode.EMBED,
                optionsBuilder.build()
        );

        return embeddingModel;
    }

    /**
     * 创建 Ollama Embedding 模型
     */
    private EmbeddingModel createOllama(AiModelContext context, Integer dimension) {
        AiModel model = context.getModel();
        AiProvider provider = context.getProvider();

        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(provider.getApiBaseUrl())
                .build();

        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
                .model(model.getModelCode())
                .dimensions(dimension)
                .build();

        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    /**
     * 校验知识库指定的向量维度
     */
    private void validateDimension(AiModel model, Integer dimension) {

        if (dimension == null) {
            return;
        }

        if (model.getSupportedDimensions() == null
                || !model.getSupportedDimensions().contains(dimension)) {
            throw new BusinessException("当前 Embedding 模型不支持指定的向量维度");
        }
    }

    /**
     * 根据模型 ID 创建 Embedding 客户端
     */
    public AiEmbeddingContext create(Long modelId, Integer dimension) {
        AiModelContext context = aiModelService.getAvailableModelContext(
                AiModelType.EMBEDDING.getCode(),
                modelId
        );
        validateDimension(context.getModel(), dimension);
        EmbeddingModel embeddingModel = create(context, dimension);

        return new AiEmbeddingContext(
                embeddingModel,
                context.getModel(),
                context.getProvider()
        );
    }
}
