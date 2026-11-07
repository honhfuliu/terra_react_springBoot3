package com.ziheng.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
/**
 * AI 模块统一配置
 * <p>
 * 负责管理 AI 平台自身的业务配置，
 * 不替代 Spring AI 底层模型配置。
*/
@ConfigurationProperties(prefix = "terra.ai")
public class AiProperties {

    /**
     * 默认模型供应商
     */
    private String defaultProvider = "dashscope";

    /**
     * Chat 对话配置
     */
    private Chat chat = new Chat();

    /**
     * Embedding 向量模型配置
     */
    private Embedding embedding = new Embedding();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    /**
     * Chat 功能配置
     */
    public static class Chat {

        /**
         * 是否启用 Chat 功能
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Embedding 功能配置
     */
    public static class Embedding {

        /**
         * 是否启用 Embedding 功能
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
